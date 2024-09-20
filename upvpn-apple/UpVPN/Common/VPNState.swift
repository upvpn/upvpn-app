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
    case accepted(Location)
    case serverCreated(Location)
    case serverRunning(Location)
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
}
