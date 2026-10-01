//
//  TunnelManagerSimulator.swift
//  UpVPN
//
//  Created by Himanshu on 8/19/24.
//

import Foundation
import WireGuardKit

#if targetEnvironment(simulator)
actor TunnelManager {
    static let shared = TunnelManager()

    /// Status of UpVPN tunnel
    @Published var tunnelStatus: TunnelStatus = TunnelStatus.loading

    /// Last disconnect error
    @Published var lastError: String?

    /// WireGuard config at runtime when tunnel is connected
    @Published var runtimeConfig: TunnelConfiguration?

    private var interface: InterfaceConfiguration

    private var peer = PeerConfiguration(publicKey: PrivateKey().publicKey)

    private var stateTransitionTask: Task<Void, Never>? = nil
    private var runtimeConfigTask: Task<Void, Never>? = nil


    init() {
        self.interface = InterfaceConfiguration(privateKey: PrivateKey())
        self.interface.addresses = [IPAddressRange(from: "10.1.2.3/32")!]
        self.interface.dns = [DNSServer(from: "1.1.1.1")!]
        self.interface.listenPort = 52123

        peer.endpoint = Endpoint(from: "1.2.3.4:51820")!
        peer.allowedIPs = [IPAddressRange(from: "0.0.0.0/0")!, IPAddressRange(from: "::/0")!]
        peer.persistentKeepAlive = 25
    }

    func setBaseTunnelConfiguration(tunnelConfiguration: TunnelConfiguration) {
        //
    }

    private func stopAndCleanup() {
        stateTransitionTask?.cancel()
        stateTransitionTask = nil
        runtimeConfigTask?.cancel()
        runtimeConfigTask = nil
        runtimeConfig = nil
        peer.txBytes = 0
        peer.rxBytes = 0
    }

    private func startRuntimeConfigTask() {
        self.runtimeConfigTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                if peer.txBytes != nil {
                    peer.txBytes! += 500
                } else {
                    peer.txBytes = 500
                }

                if peer.rxBytes != nil {
                    peer.rxBytes! += 1000
                } else {
                    peer.rxBytes = 1000
                }
                self.runtimeConfig =  TunnelConfiguration(name: "UpVPN", interface: interface, peers: [peer])
            }
        }
    }


    func reload() async throws {
        tunnelStatus = TunnelStatus.disconnected
    }

    func start(to location: Location) async throws {
        self.stopAndCleanup()
        let requestedAt = Date.now
        stateTransitionTask = Task {
            guard !Task.isCancelled else { return }
            do {
                self.tunnelStatus = TunnelStatus.requesting(location)
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.accepted(location, requestedAt)
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.serverCreated(location, requestedAt)
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.serverRunning(location, requestedAt)
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.serverReady(location)
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.connecting(location)
                self.startRuntimeConfigTask()
                try await self.waitForNextStep()
                self.tunnelStatus = TunnelStatus.connected(location, Date())
            } catch {
                // Stop advancing when an early stop cancels provisioning.
                return
            }

        }
    }

    /// Time taken by a step of session setup. Throws when the state transition task
    /// is cancelled by stop, even if the sleep itself completed just before that,
    /// so that the task does not move status past disconnecting
    private func waitForNextStep() async throws {
        try await Task.sleep(nanoseconds: 1_000_000_000)
        try Task.checkCancellation()
    }

    func stop() async {
        self.stopAndCleanup()
        self.tunnelStatus = self.tunnelStatus.toDisconnecting()

        if !self.tunnelStatus.isDisconnected() {
            try? await Task.sleep(nanoseconds: 1_000_000_000)
        }

        self.tunnelStatus = TunnelStatus.disconnected
    }

    func clearLastError() {
        self.lastError = nil
    }
}
#endif
