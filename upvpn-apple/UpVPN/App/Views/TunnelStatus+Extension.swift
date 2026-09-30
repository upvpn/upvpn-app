//
//  TunnelStatus+Extension.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import SwiftUI

extension TunnelStatus {
    func displayText() -> String {
        switch self {
        case .loading:
            "Loading"
        case .disconnected:
            "VPN is off"
        case .requesting(_):
            "Requesting"
        case .accepted(_, _):
            "Accepted"
        case .serverCreated(_, _):
            "Server Created"
        case .serverRunning(_, _):
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

/// Enables VPN toggle when tunnel is disconnected or connected, and also
/// when user can end the session that is still being setup
struct VPNToggleEnabled: ViewModifier {
    var tunnelStatus: TunnelStatus
    var endSessionThreshold: TimeInterval = TunnelStatus.endSessionThreshold

    // endAllowedDate that is known to have been reached. It is compared with
    // endAllowedDate of current status hence cannot enable toggle for any other session
    @State private var reachedEndAllowedDate: Date? = nil

    func body(content: Content) -> some View {
        let endAllowedDate = tunnelStatus.endAllowedDate(threshold: endSessionThreshold)
        let isEndAllowed = endAllowedDate.map { $0 == reachedEndAllowedDate || $0 <= Date() } ?? false

        content
            .disabled(!(tunnelStatus.isDisconnectedOrConnected() || isEndAllowed))
            // nothing else is guaranteed to update the view when endAllowedDate is reached
            .task(id: endAllowedDate) {
                await waitUntilReached(endAllowedDate)
            }
    }

    @MainActor
    private func waitUntilReached(_ endAllowedDate: Date?) async {
        guard let endAllowedDate = endAllowedDate else { return }

        let remaining = endAllowedDate.timeIntervalSinceNow
        if remaining > 0 {
            // session is requested in network extension according to wall clock, which could
            // have changed since. No matter what, the wait is never longer than the threshold.
            let seconds = min(remaining, endSessionThreshold > 0 ? endSessionThreshold : 0)
            // nil when not representable, for example infinite threshold which never allows to end
            guard let nanoseconds = UInt64(exactly: (seconds * 1_000_000_000).rounded()) else { return }
            do {
                try await Task.sleep(nanoseconds: nanoseconds)
            } catch {
                // cancelled: view is gone or tunnel status has a different endAllowedDate
                return
            }
        }

        reachedEndAllowedDate = endAllowedDate
    }
}
