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
    // Date is when the session was requested
    case accepted(Location, Date)
    case serverCreated(Location, Date)
    case serverRunning(Location, Date)
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
        case .accepted(let location, let requestedAt):
            return "accepted \(location.city) \(requestedAt)"
        case .serverCreated(let location, let requestedAt):
            return "serverCreated \(location.city) \(requestedAt)"
        case .serverRunning(let location, let requestedAt):
            return "serverRunning \(location.city) \(requestedAt)"
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
        case .accepted(let location, let requestedAt):
                .accepted(location, requestedAt)
        case .serverCreated(let location, let requestedAt):
                .serverCreated(location, requestedAt)
        case .serverRunning(let location, let requestedAt):
                .serverRunning(location, requestedAt)
        case .serverReady(let location):
                .serverReady(location)
        }
    }


    func toDisconnecting() -> Self {
        return switch self {
        case .disconnected, .loading:
            self
        case .requesting(let location),
                .accepted(let location, _),
                .serverCreated(let location, _),
                .serverRunning(let location, _),
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
            .accepted(let location, _),
            .serverCreated(let location, _),
           .serverRunning(let location, _),
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

extension TunnelStatus {
    /// Seconds since a session was requested after which user can end the session
    /// while it is still being setup i.e. when accepted, serverCreated or serverRunning
    #if targetEnvironment(simulator)
    static let endSessionThreshold: TimeInterval = 1
    #else
    static let endSessionThreshold: TimeInterval = 10
    #endif

    /// Date from which user can end the session that is still being setup,
    /// nil when session in current status cannot be ended
    func endAllowedDate(threshold: TimeInterval = TunnelStatus.endSessionThreshold) -> Date? {
        switch self {
        case .accepted(_, let requestedAt),
                .serverCreated(_, let requestedAt),
                .serverRunning(_, let requestedAt):
            // negative (or NaN) threshold is same as no threshold
            return requestedAt.addingTimeInterval(threshold > 0 ? threshold : 0)
        case .loading, .disconnected, .requesting, .serverReady, .connecting, .connected, .disconnecting:
            // requesting has no session yet, serverReady and connecting are quick to get connected
            return nil
        }
    }
}
