//
//  TunnelStatus+Extension.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import Foundation

extension TunnelStatus {
    func displayText() -> String {
        switch self {
        case .loading:
            "Loading"
        case .disconnected:
            "VPN is off"
        case .requesting(_):
            "Requesting"
        case .accepted(_):
            "Accepted"
        case .serverCreated(_):
            "Server Created"
        case .serverRunning(_):
            "Server Running"
        case .serverReady(_):
            "Server Ready"
        case .connecting(_):
            "Connecting"
        case .connected(_, _):
            "VPN is on"
        case .disconnecting(_):
            "Disconnecting"
        }
    }

    func shieldSystemImage() -> String {
        return switch self {
        case .connected(_, _):
            "checkmark.shield"
        default:
            "shield.slash"
        }
    }

    func progress() -> Float {
        return switch self {
        case .loading, .disconnected, .disconnecting:
            0
        case .requesting:
            0.1
        case .accepted:
            0.25
        case .serverCreated:
            0.5
        case .serverRunning:
            0.75
        case .serverReady:
            0.8
        case .connecting:
            0.95
        case .connected:
            1
        }
    }

    func isDisconnectedOrConnected() -> Bool {
        return switch self {
        case .disconnected, .connected:
            true
        default:
            false
        }
    }
}
