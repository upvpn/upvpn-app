//
//  ReviewManager.swift
//  UpVPN
//

import SwiftUI

class ReviewManager: ObservableObject {
    static let minimumConnectCount = 2
    static let delaySeconds: Double = 2
    #if DEBUG
    static let minimumDaysBetweenRequests = 0
    #else
    static let minimumDaysBetweenRequests = 60
    #endif

    @AppStorage("reviewConnectCount") private var connectCount = 0
    @AppStorage("reviewLastRequestDate") private var lastRequestDate: Double = 0

    @Published var shouldRequestReview = false

    /// Call when the VPN transitions from connected to disconnecting.
    /// Increments the session count and sets ``shouldRequestReview`` after a delay of ``delaySeconds``
    /// if the user has completed at least ``minimumConnectCount`` sessions and
    /// at least ``minimumDaysBetweenRequests`` days have passed since the last review request.
    func onDisconnectedFromConnected() {
        connectCount += 1
        guard connectCount >= Self.minimumConnectCount else { return }

        if lastRequestDate > 0 {
            let lastDate = Date(timeIntervalSince1970: lastRequestDate)
            guard let daysAgo = Calendar.current.date(byAdding: .day, value: -Self.minimumDaysBetweenRequests, to: Date()),
                  lastDate < daysAgo else { return }
        }

        lastRequestDate = Date().timeIntervalSince1970
        Task { @MainActor in
            try? await Task.sleep(nanoseconds: UInt64(Self.delaySeconds * 1_000_000_000))
            shouldRequestReview = true
        }
    }
}
