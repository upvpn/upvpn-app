//
//  ReviewRequestView.swift
//  UpVPN
//

#if os(iOS) || os(macOS)
import SwiftUI
import StoreKit

@available(iOS 17, macOS 13, *)
struct ReviewRequestView: View {
    @Environment(\.requestReview) private var requestReview
    @Binding var shouldRequest: Bool

    var body: some View {
        EmptyView()
            .onChange(of: shouldRequest) {
                if shouldRequest {
                    requestReview()
                    shouldRequest = false
                }
            }
    }
}
#endif
