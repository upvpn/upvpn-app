//
//  VPNOrchestratorState.swift
//  UpVPN
//
//  Created by Himanshu on 7/23/24.
//

import Foundation
import WireGuardKit

enum StateTransitionError : Error {
    case invalidTransition(String)
}

extension StateTransitionError: CustomStringConvertible {
    var description: String {
        switch self {
        case .invalidTransition(let string):
            return string
        }
    }
}

enum VPNOrchestratorState {
    case disconnected
    case requesting(requestId: UUID, location: Location)
    case accepted(location: Location, accepted: Accepted, interface: InterfaceConfiguration)
    case serverCreated(location: Location, serverCreated: ServerCreated, interface: InterfaceConfiguration)
    case serverRunning(location: Location, serverRunning: ServerRunning, interface: InterfaceConfiguration)
    case serverReady(location: Location, serverReady: ServerReady, tunnelConfiguration: TunnelConfiguration)
    case connecting(location: Location, serverReady: ServerReady, tunnelConfiguration: TunnelConfiguration)
    case connected(location: Location, serverReady: ServerReady, tunnelConfiguration: TunnelConfiguration, date: Date)
    case disconnecting(location: Location)
}

extension VPNOrchestratorState {
    func toVPNState() -> VPNState {
        return switch self {
        case .disconnected:
            .disconnected
        case .requesting(_, let location):
            .requesting(location)
        case .accepted(let location, _, _):
                .accepted(location)
        case .serverCreated(let location, _, _):
                .serverCreated(location)
        case .serverRunning(let location, _, _):
                .serverRunning(location)
        case .serverReady(let location, _, _):
                .serverReady(location)
        case .connecting(let location, _, _):
                .connecting(location)
        case .connected(let location, _, _, let date):
                .connected(location, date)
        case .disconnecting(let location):
                .disconnecting(location)
        }
    }

    func toDisconnecting() -> Self {
        return switch self {
        case .disconnected:
                .disconnected
        case .requesting(_, let location):
                .disconnecting(location: location)
        case .accepted(let location, _, _):
                .disconnecting(location: location)
        case .serverCreated(let location, _, _):
                .disconnecting(location: location)
        case .serverRunning(let location, _, _):
                .disconnecting(location: location)
        case .serverReady(let location, _, _):
                .disconnecting(location: location)
        case .connecting(let location, _, _):
                .disconnecting(location: location)
        case .connected(let location, _, _, _):
                .disconnecting(location: location)
        case .disconnecting(_):
                self
        }
    }

    func isDisconnectingOrDisconnected() -> Bool {
        switch self {
        case .disconnected, .disconnecting:
            return true
        default:
            return false
        }
    }

    func isDisconnected() -> Bool {
        if case .disconnected = self {
            return true
        }
        return false
    }

    // only allowed transition is from server ready
    func transitionToConnecting() -> Result<Self, StateTransitionError> {
        return switch self {
        case .serverReady(let location, let serverReady, let tunnelConfiguration):
                .success(.connecting(location: location, serverReady: serverReady, tunnelConfiguration: tunnelConfiguration))
        default:
                .failure(.invalidTransition("invalid transition to connecting"))
        }
    }

    // only allowed transition is from connecting
    func transitionToConnected(_ date: Date) -> Result<Self, StateTransitionError> {
        return switch self {
        case .connecting(let location, let serverReady, let tunnelConfiguration):
                .success(.connected(location: location, serverReady: serverReady, tunnelConfiguration: tunnelConfiguration, date: date))
        default:
                .failure(.invalidTransition("invalid transition to connected"))
        }
    }


    // compute new state from server update, only move state forward
    func newStateFromUpdate(status: VpnSessionStatus, location: Location) -> VPNOrchestratorState? {
        return switch status {
        case .accepted:
            switch self {
            case .accepted: self // no update
            default: nil
            }
        case .failed:
                .disconnected
        case .serverCreated(let serverCreated):
            switch self {
            case .accepted(_, _, let interface):
                    .serverCreated(location: location, serverCreated: serverCreated, interface: interface)
            default: nil
            }
        case .serverRunning(let serverRunning):
            switch self {
            case .accepted(_, _, let interface), .serverCreated(_, _, let interface):
                    .serverRunning(location: location, serverRunning: serverRunning, interface: interface)
            case .serverRunning:
                self // no update
            default:
                nil
            }
        case .serverReady(let serverReady):
            switch self {
            case .accepted(_, _, let interface), .serverCreated(_, _, let interface), .serverRunning(_, _, let interface):
                    .serverReady(location: location,
                                 serverReady: serverReady,
                                 tunnelConfiguration: TunnelConfiguration.from(serverReady: serverReady,
                                                                               interface: interface))
            case .serverReady:
                self // no update
            default:
                nil
            }
        default: nil
        }
    }


    // When asked to stop this answers in tuple
    // (new state, should it stop exisiting WG tunnel, meta for caller to end vpn session in progress)
    func transitionOnStop() -> (Self, Bool, SessionMeta?) {
        return switch self {
        case .disconnected:
            (self, false, nil)
        case .requesting(let requestId, _):
            (.disconnected, false, SessionMeta(requestId: requestId))
        case .accepted(_, let accepted, _):
            (.disconnected, false, SessionMeta(requestId: accepted.requestId, vpnSessionUuid: accepted.vpnSessionUuid))
        case .serverCreated(_, let serverCreated, _):
            (.disconnected, false, SessionMeta(requestId: serverCreated.requestId, vpnSessionUuid: serverCreated.vpnSessionUuid))
        case .serverRunning(_, let serverRunning, _):
            (.disconnected, false, SessionMeta(requestId: serverRunning.requestId, vpnSessionUuid: serverRunning.vpnSessionUuid))
        case .serverReady(_, let serverReady, _):
            (.disconnected, false, SessionMeta(requestId: serverReady.requestId, vpnSessionUuid: serverReady.vpnSessionUuid))
        case .connecting(let location, let serverReady, _):
            (.disconnecting(location: location), true, SessionMeta(requestId: serverReady.requestId, vpnSessionUuid: serverReady.vpnSessionUuid))
        case .connected(let location, let serverReady, _, _):
            (.disconnecting(location: location), true, SessionMeta(requestId: serverReady.requestId, vpnSessionUuid: serverReady.vpnSessionUuid))
        case .disconnecting(let location):
            (.disconnecting(location: location), false, nil)
        }
    }
}
