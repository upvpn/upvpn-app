//
//  TunnelManager.swift
//  UpVPN
//
//  Created by Himanshu on 6/27/24.
//

import Foundation
import NetworkExtension
import WireGuardKit

#if !targetEnvironment(simulator)
/// Manages a single VPN configuration NETunnelProviderManager
/// and its lifecycle
actor TunnelManager {
    static let shared = TunnelManager()

    /// Status of UpVPN tunnel
    @Published var tunnelStatus: TunnelStatus = TunnelStatus.loading

    /// Last disconnect error
    @Published var lastError: String?

    /// WireGuard config at runtime when tunnel is connected
    @Published var runtimeConfig: TunnelConfiguration?

    private var startRequestId: String? = nil

    // This is the information that is based on custom configuration
    // such as device IP , dns servers etc. This is used to be
    // merged with runtime configuration to generate full runtime configuration
    private var baseTunnelConfiguration: TunnelConfiguration?

    private var notificationObserverTask: Task<Void, Never>?
    private var statusTask: Task<Void, Never>?
    private var runtimeConfigTask: Task<Void, Never>?


    private init() {
        Task {
            try await reload()
            await setupNotificationObservers()
        }
    }

    /// Platform native status
    private var vpnStatus: NEVPNStatus? {
        didSet {
            refreshTunnelStatus()
        }
    }

    private var manager: NETunnelProviderManager? {
        willSet {
            self.tunnelStatus = TunnelStatus.loading
        }
        didSet {
            refreshVpnStatus()
        }
    }

    private func refreshVpnStatus() {
        self.vpnStatus = self.manager?.connection.status
    }

    private func refreshTunnelStatus() {
        if let vpnStatus = self.vpnStatus {
            switch vpnStatus {
            case .invalid:
                self.tunnelStatus = .disconnected
                stopStatusTask()
            case .disconnected:
                self.tunnelStatus = .disconnected
                self.runtimeConfig = nil
                stopStatusTask()
            case .connecting:
                startStatusTask()
            case .connected:
                // dont stop task here for it to get status that its connected
                // task should self stop when it sees a terminal state
                startRuntimeConfigTask()

                // also start task here for when UI is launched and Tunnel was already connected
                // to move .loading tunnel status forward
                startStatusTask()
            case .disconnecting:
                self.tunnelStatus = self.tunnelStatus.toDisconnecting()
                stopStatusTask()
                stopRuntimeConfigTask()
            case .reasserting:
                fatalError("cannot process reasserting")
            @unknown default:
                fatalError("unknown NEVPNStatus: \(self)")
            }
        } else {
            self.tunnelStatus = TunnelStatus.disconnected
        }
    }

    private func startRuntimeConfigTask() {
        if runtimeConfigTask == nil {
            runtimeConfigTask = Task {
            loop: while true {
                    if Task.isCancelled { break }
                    try? await Task.sleep(nanoseconds: 1000_000_000)
                    guard let session = self.manager?.connection as? NETunnelProviderSession else { return }
                    do {
                        if let response = try await session.sendProviderMessage(request: Request.getRuntimeConfiguration) {
                            switch response {
                            case .runtimeConfiguration(let config):
                                if let config = config {
                                    self.runtimeConfig = try? TunnelConfiguration(fromUapiConfig: config, basedOn: baseTunnelConfiguration)
                                }
                            default:
                                print("")
                            }
                        }
                    } catch {
                        print("error sending provider message: \(error)")
                    }
                }
            }
        }
    }

    private func stopRuntimeConfigTask() {
        if let runtimeConfigTask = runtimeConfigTask {
            runtimeConfigTask.cancel()
        }
        runtimeConfigTask = nil
        runtimeConfig = nil
    }

    private func startStatusTask() {
        if statusTask == nil {
            statusTask = Task {
            loop: while true {
                    if Task.isCancelled { break }
                    try? await Task.sleep(nanoseconds: 500_000_000)
                    guard let session = self.manager?.connection as? NETunnelProviderSession else { return }
                    do {
                        if let response = try await session.sendProviderMessage(request: Request.status) {
                            switch response {
                            case .status(let state):
                                // we only care about states from orchestrator in
                                // requesting, accepted, serverCreated, serverRunning, serverReady, connecting, and connected
                                // rest of the states are get brought in by native NEVPNStatus
                                // why? because when client is in "requesting" and NE hasn't yet transitioned to "requesting"
                                // and still in "disconnected" will update tunnel status incorrectly.
                                switch state {
                                case .disconnecting(_), .disconnected:
                                    continue
                                default:
                                    self.updateTunnelStatus(status: TunnelStatus.fromVPNState(state))
                                }

                                // task should stop itself when reaching a terminal state
                                // when connection.status reaches connected this task may not yet
                                // have received the updated state here, hence self terminate after updating local state
                                if case .connected = state {
                                    break loop
                                }
                                // Dont self stop on receiving disconnected because TunnelManager would do that
                                // Also when requesting a new startTunnel - the response from sendProviderMessage
                                // may receive .disconnected before the call to NE to start
//                                if case .disconnected = state {
//                                    print("exiting state task - because disconnected")
//                                    break loop
//                                }
                            default:
                                print("")
                            }
                        }
                    } catch {
                        print("error sending provider message: \(error)")
                    }
                }
            }
        }
    }

    private func stopStatusTask() {
        if let statusTask = statusTask {
            statusTask.cancel()
        }
        statusTask = nil
    }

    private func updateTunnelStatus(status: TunnelStatus) {
        self.tunnelStatus = status
    }

    func setBaseTunnelConfiguration(tunnelConfiguration: TunnelConfiguration) {
        self.baseTunnelConfiguration = tunnelConfiguration
    }

    func reload() async throws {
        // To make sure when reload is triggered from UI (onAppear)
        // old tasks are cleaned up
        // onAppear seems to be called again when window is closed and opened again on macOS 12
        // but onAppear is not called again on macOS14
        cleanupTempTasks()
        let managers = try await NETunnelProviderManager.loadAllFromPreferences()
        self.manager = managers.first
    }

    private func create() async throws {
        // Based on https://developer.apple.com/forums/thread/674686?answerId=663891022#663891022

        let managers = try await NETunnelProviderManager.loadAllFromPreferences()
        let manager = managers.first ?? NETunnelProviderManager()

        let providerProtocol = NETunnelProviderProtocol()
        providerProtocol.providerBundleIdentifier = Bundle.main.bundleIdentifier! + ".network-extension"
        providerProtocol.providerConfiguration = [:]
        providerProtocol.serverAddress = "dynamic"

        manager.protocolConfiguration = providerProtocol
        manager.isEnabled = true
        manager.isOnDemandEnabled = false
        manager.localizedDescription = "Serverless VPN"

        try await manager.saveToPreferences()
        try await manager.loadFromPreferences()
        self.manager = manager
    }

    private func setupNotificationObservers() {
        self.notificationObserverTask = Task {
            await withTaskGroup(of: Void.self) { group in
                group.addTask { [weak self] in
                    for await _ in NotificationCenter.default.notifications(named: .NEVPNConfigurationChange) {
                        if Task.isCancelled { break }
                        do {
                            try await self?.onConfigChanged()
                        } catch {
                            // todo
                        }
                    }
                }

                group.addTask { [weak self] in
                    for await notification in NotificationCenter.default.notifications(named: .NEVPNStatusDidChange) {
                        if Task.isCancelled { break }
                        await self?.onStatusChanged(notification.object as? NETunnelProviderSession)
                    }
                }

                await group.waitForAll()
            }
        }
    }

    private func onConfigChanged() async throws {
        //try await self.reload()
    }

    private func onStatusChanged(_ session: NETunnelProviderSession?) async {
        if let session = session {

            // only get last error for current transition, otherwise you get same persisted last error all the time
            if session.status == .disconnected && (self.vpnStatus == .connecting || self.vpnStatus == .disconnecting) {

                if let startRequestId = self.startRequestId {
                    self.lastError = lastErrorFromNetworkExtension(startRequestId: startRequestId)
                }

                /* Because we want to support older versions, a file is used to communicate error.

                if #available(iOS 16.0, macOS 13.0, tvOS 17.0, *) {
                    do {
                        try await session.fetchLastDisconnectError()
                    } catch {
                        self.lastError = error
                    }
                }
                 */
            }

            if session.status == .invalid {
                // This happens when configuration is removed outside the app, from System Settings.
                // no need to refresh status here because manager's didSet will do that.
                self.manager = nil
            } else {
                self.refreshVpnStatus()
            }
        }
    }

    private func enable() async throws {
        if let manager = self.manager, !manager.isEnabled {
            manager.isEnabled = true
            try await manager.saveToPreferences()
            try await manager.loadFromPreferences()
            self.manager = manager
        }
    }

    func start(to location: Location) async throws {
        self.clearLastError()
        
        if self.manager == nil {
            try await self.create()
        }

        // if another vpn app has its configuration enabled then ours is disabled
        // so when user request to connect try to enable our configuration
        try await self.enable()

        self.tunnelStatus = TunnelStatus.requesting(location)
        do {
            startRequestId = UUID().uuidString
            var options = (try? location.asDictionary()) ?? [:]
            options["startRequestId"] = startRequestId
            print("startTunnel: startRequestId:\(startRequestId!) city:\(location.city)")
            try (self.manager?.connection as? NETunnelProviderSession)?.startTunnel(options: options)
        } catch let error {
            print("cannot start: \(error)")
            throw error
        }
    }

    func stop() {
        self.tunnelStatus = self.tunnelStatus.toDisconnecting()
        self.manager?.connection.stopVPNTunnel()
    }

    func clearLastError() {
        self.lastError = nil
    }

    private func cleanupTempTasks() {
        runtimeConfigTask?.cancel()
        statusTask?.cancel()
        runtimeConfigTask = nil
        statusTask = nil
    }

    deinit {
        runtimeConfigTask?.cancel()
        statusTask?.cancel()
        notificationObserverTask?.cancel()
        runtimeConfigTask = nil
        statusTask = nil
        notificationObserverTask = nil
    }
}

private func lastErrorFromNetworkExtension(startRequestId: String) -> String? {
    guard let lastErrorFileURL = FileManager.networkExtensionLastErrorFileURL else { return nil }
    guard let lastErrorData = try? Data(contentsOf: lastErrorFileURL) else { return nil }
    guard let lastErrorStrings = String(data: lastErrorData, encoding: .utf8)?.split(separator: "\n") else { return nil }
    guard lastErrorStrings.count == 2 && startRequestId == lastErrorStrings[0] else { return nil }

    return String(lastErrorStrings[1])
}
#endif
