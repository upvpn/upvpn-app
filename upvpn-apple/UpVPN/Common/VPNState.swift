//
//  VPNState.swift
//  UpVPN
//
//  Created by Himanshu on 7/5/24.
//

import Foundation

enum VPNState : Codable {
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

extension VPNState {
    func toDisconnecting() -> Self {
        return switch self {
        case .disconnected:
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
}
