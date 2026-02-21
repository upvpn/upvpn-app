//
//  VPNState.swift
//  UpVPN
//
//  Created by Himanshu on 6/27/24.
//

import Foundation

enum TunnelStatus: Equatable {
    case loading
    case disconnected
    case requesting(Location)
    case accepted(Location)
    case serverCreated(Location)
    case serverRunning(Location)
    case serverReady(Location)
    case connecting(Location)
    case connected(Location, Date)
    case disconnecting(Location)
}


extension TunnelStatus : CustomStringConvertible {
    var description: String {
        switch self {
        case .loading:
            return "loading"
        case .requesting(let location):
            return "requesting \(location.city)"
        case .disconnected:
            return "disconnected"
        case .connecting(let location):
            return "connecting \(location.city)"
        case .connected(let location, let date):
            return "connected \(location.city) \(date)"
        case .disconnecting(let location):
            return "disconnecting \(location.city)"
        case .accepted(let location):
            return "accepted \(location.city)"
        case .serverCreated(let location):
            return "serverCreated \(location.city)"
        case .serverRunning(let location):
            return "serverRunning \(location.city)"
        case .serverReady(let location):
            return "serverReady \(location.city)"
        }
    }
}

extension TunnelStatus {
    static func fromVPNState(_ vpnState: VPNState) -> Self {
        switch vpnState {
        case .disconnected:
                .disconnected
        case .requesting(let location):
                .requesting(location)
        case .connecting(let location):
                .connecting(location)
        case .connected(let location, let date):
                .connected(location, date)
        case .disconnecting(let location):
                .disconnecting(location)
        case .accepted(let location):
                .accepted(location)
        case .serverCreated(let location):
                .serverCreated(location)
        case .serverRunning(let location):
                .serverRunning(location)
        case .serverReady(let location):
                .serverReady(location)
        }
    }


    func toDisconnecting() -> Self {
        return switch self {
        case .disconnected, .loading:
            self
        case .requesting(let location),
                .accepted(let location),
                .serverCreated(let location),
                .serverRunning(let location),
                .serverReady(let location),
                .connecting(let location),
                .connected(let location, _),
                .disconnecting(let location):
                .disconnecting(location)
        }
    }

    func isConnected() -> Bool {
        if case .connected = self {
            return true
        }
        return false
    }

    func isDisconnected() -> Bool {
        if case .disconnected = self {
            return true
        }
        return false
    }

    func currentLocation() -> Location? {
        return switch self {
        case .loading, .disconnected:
            nil
        case .requesting(let location),
            .accepted(let location),
            .serverCreated(let location),
           .serverRunning(let location),
           .serverReady(let location),
           .connecting(let location),
           .connected(let location, _),
           .disconnecting(let location):
            location
        }
    }

    func connectedDate() -> Date? {
        return switch self {
        case .connected(_, let date):
            date
        default:
            nil
        }
    }

    func shouldToggleBeOn() -> Bool {
        return switch self {
        case .disconnected, .disconnecting, .loading:
            false
        default:
            true
        }
    }
}
