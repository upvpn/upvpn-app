//
//  VPNOrchestrator.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//

import Foundation
import WireGuardKit
import os.log
import NetworkExtension

enum OrchestratorError: Error {
    case wireguardAdapterError(WireGuardAdapterError)
    case vpnSessionRepositoryError(VPNSessionRepositoryError)
    case stateTransitionError(StateTransitionError)
    case invalid(String)
}

extension OrchestratorError: CustomStringConvertible {
    var description: String {
        return switch self {
        case .wireguardAdapterError(let wireGuardAdapterError):
            wireGuardAdapterError.description
        case .vpnSessionRepositoryError(let vpnSessionRepositoryError):
            vpnSessionRepositoryError.description
        case .stateTransitionError(let stateTransitionError):
            stateTransitionError.description
        case .invalid(let string):
            string
        }
    }
}

actor VPNOrchestrator {
    private var commandStream: AsyncStream<Any>
    private var continuation: AsyncStream<Any>.Continuation
    private var mainTask: Task<Void, Never>? = nil
    private var shutdownRequested = false

    private var orchestratorState: VPNOrchestratorState = VPNOrchestratorState.disconnected
    private var vpnSessionRepository: VPNSessionRepository = DefaultVPNSessionRepository()

    private var packetTunnelProvider: NEPacketTunnelProvider

    private lazy var wgAdapter: WireGuardAdapter = {
        return WireGuardAdapter(with: self.packetTunnelProvider) { logLevel, message in
            wg_log(OSLogType.info, message: message)
        }
    }()

    private lazy var wgAdapterAsync: WGAdapterAsync = {
        return WGAdapterAsync(wireguardAdapter: wgAdapter)
    }()

    // Since there is not push mechanism to client (UI), the packet tunnel provider
    // will poll orchestrator for updates after getting accepted
    // but there can be errors after acceptece on server or in the WG adapter
    // which are stored here:
    var lastErrorAfterAccepted: OrchestratorError? = nil

    init(packetTunnelProvider: NEPacketTunnelProvider) {
        (commandStream, continuation) = AsyncStream.makeStream(
            of: Any.self,
            bufferingPolicy: .unbounded
        )
        self.packetTunnelProvider = packetTunnelProvider

        Task {
            await self.startMain()
        }
    }

    private func startMain() {
        self.mainTask = Task {
            await main()
        }
    }

    private func main() async {
        for await commandBox in commandStream {
            guard let box = commandBox as? AnyCommandBox else {continue}
            await box.execute(in: self)
        }
    }


    func sendCommand<T: OrchestratorCommand>(_ command: T) async -> T.Response {
        await withCheckedContinuation { (continuation: CheckedContinuation<T.Response, Never>) in
            let box = CommandBox(command: command, continuation: continuation)

            Task {
                while !shutdownRequested {
                    let result = self.continuation.yield(box)
                    switch result {
                    case .enqueued:
                        return
                    case .dropped(_):
                        // .unbonded, yet still handle it
                        os_log("command dropped unexpectedly")
                    case .terminated:
                        os_log("stream has been terminated")
                        return
                    @unknown default:
                        break
                    }

                    // yield did not succeed try again
                    try? await Task.sleep(nanoseconds: 1_000_000) // 1ms
                }
            }

        }
    }

    func shutdown() {
        shutdownRequested = true
        continuation.finish()
        mainTask?.cancel()
    }

    deinit {
        mainTask?.cancel()
        mainTask = nil
    }

    private func updateOrchestratorState(_ newState: VPNOrchestratorState) {
        self.orchestratorState = newState
    }

    nonisolated func startAndWait(location: Location) async -> Result<(), OrchestratorError> {
        let task = Task.detached {
            let result = await self.sendCommand(Start(location: location))

            guard case .success = result else {
                return result
            }
            os_log("accepted, waiting for connection")

            // wait for local client to connect
            while true {
                try? await Task.sleep(nanoseconds: 500_000_000)
                let state = await self.getStatus()

                // state could be in terminal state other than connected if:
                // there was an error or tunnel was asked to stop
                switch state {
                case .disconnected:
                    // there was an error or tunnel was asked to stop
                    if let error = await self.lastErrorAfterAccepted {
                        return .failure(error)
                    } else {
                        return .failure(.invalid("cannot setup VPN, please try again"))
                    }
                case .requesting, .accepted, .serverCreated, .serverRunning, .serverReady, .connecting, .disconnecting:
                    continue
                case .connected:
                    return .success(())
                }
            }
        }
        return await task.value
    }

    // only run by mainTask
    func start(location: Location) async -> Result<(), OrchestratorError> {
        // check if no other session is already in progress
        if !self.orchestratorState.isDisconnected() {
            return .failure(.invalid("VPN session is already in progress"))
        }

        let requestId = UUID()
        self.updateOrchestratorState(.requesting(requestId: requestId, location: location))

        // create new vpn session and start watcher
        let result = await self.vpnSessionRepository.newVpnSession(requestId: requestId, location: location) { (vpnSessionStatus, location) in
            // watcher will use this callback to send updates back to orchestrator
            Task {
                await self.sendCommand(ServerStatusUpdate(newStatus: vpnSessionStatus, location: location))
            }
        }.mapError(OrchestratorError.vpnSessionRepositoryError)


        guard case .success((let accepted, let interfaceConfiguration)) = result else {
            // update state
            os_log("failed new vpn session YO")
            self.updateOrchestratorState(.disconnected)
            return result.map { _ in () }
        }

        // update orchestrator state to accepted
        self.updateOrchestratorState(
            VPNOrchestratorState.accepted(location: location, accepted: accepted, interface: interfaceConfiguration))

        return .success(())
    }

    // only run in mainTask
    private func connect(tunnelConfiguration: TunnelConfiguration) async -> Result<(), OrchestratorError> {
        do {
            let connectingState = try self.orchestratorState.transitionToConnecting().get()
            updateOrchestratorState(connectingState)

            //todo: caller should make sure to cleanup (call stop) if this fails
            // so that local vpn session, on server and orchestrator state is restored
            try await wgAdapterAsync.start(tunnelConfiguration: tunnelConfiguration).get()

            let connectedState = try self.orchestratorState.transitionToConnected(Date.now).get()
            updateOrchestratorState(connectedState)

            return .success(())
        } catch let error as StateTransitionError {
            return .failure(.stateTransitionError(error))
        } catch let error as WireGuardAdapterError {
            return .failure(.wireguardAdapterError(error))
        }
        catch {
            // not expected only for compiler
            return .failure(.invalid("\(error.localizedDescription)"))
        }
    }

    // only run by mainTask
    func stop(reason: String) async -> Result<(), OrchestratorError> {
        // check if already disconnected
        if self.orchestratorState.isDisconnected() {
            return .success(())
        }

        // transition state to disconnecting
        let (newState, shouldStop, sessionMeta) = self.orchestratorState.transitionOnStop()
        updateOrchestratorState(newState)

        // make api call to end session in concurrent task
        var apiTask: Task<Void, Never>? = nil
        if let sessionMeta = sessionMeta {
            apiTask = Task {
                // this would update local record, make api call to end, and stop watcher
                let result = await self.vpnSessionRepository.endVpnSession(meta: sessionMeta, reason: reason)
                if case .failure(let failure) = result {
                    os_log("%{public}@", "\(failure)")
                }
            }
        }

        var result: Result<(), OrchestratorError> = .success(())
        if shouldStop {
            // only call adapter to stop only when connection was attempted
            result = await wgAdapterAsync.stop()
                .mapError(OrchestratorError.wireguardAdapterError)
            updateOrchestratorState(VPNOrchestratorState.disconnected)
        }

        // now wait for api call to complete
        if let apiTask = apiTask {
            await apiTask.value
        }

        return result
    }

    func getStatus() async -> VPNState {
        return self.orchestratorState.toVPNState()
    }

    func getRuntimeConfiguration() async -> String? {
        await wgAdapterAsync.getRuntimeConfiguration()
    }

    // only run by mainTask
    func onServerStatusUpdate(newStatus: VpnSessionStatus, location: Location) async {
        // drop any udpates that arrived after session was already ask to end
        if self.orchestratorState.isDisconnectingOrDisconnected() {
            os_log("dropping status update as tunnel is already disconnecting or disconnected")
            return
        }

        os_log("%{public}@", "new status from server: \(newStatus)")

        if case .failed = newStatus {
            os_log("vpn session failed")
            self.lastErrorAfterAccepted = OrchestratorError.invalid("Can't provision server. Please try again")
        }

        let newOrchestratorState = self.orchestratorState.newStateFromUpdate(status: newStatus, location: location)

        if let newOrchestratorState = newOrchestratorState {
            updateOrchestratorState(newOrchestratorState)

            if case .serverReady(_, _, let tunnelConfiguration) = newOrchestratorState {
                let result = await self.connect(tunnelConfiguration: tunnelConfiguration)

                // if connect failed cleanup local and server state
                if case .failure(let failure) = result {
                    os_log("%{public}@", "\(failure)")
                    let stopResult = await self.stop(reason: "client failed")
                    os_log("%{public}@", "\(stopResult)")
                }

            }
        }
    }

}
