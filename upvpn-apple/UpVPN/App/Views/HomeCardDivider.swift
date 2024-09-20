//
//  HomeCardDivider.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import SwiftUI

struct HomeCardDivider: View {
    var tunnelStatus: TunnelStatus

    var body: some View {

        switch tunnelStatus {
        case .loading, .disconnected, .disconnecting:
            Divider()
                .padding(.horizontal, 30)
        case .requesting, .accepted, .serverCreated, .serverRunning, .serverReady, .connecting:
            ProgressView(value: tunnelStatus.progress())
                .padding(.horizontal, 30)
        case .connected(_, let date):
            HStack {
                // embeded in vstack because divider in hstack becomes vertical
                VStack {
                    Divider()
                        .padding(.leading, 30)
                        .padding(.trailing, 10)
                }
                ElapsedTimeView(startDate: date)
                    .frame(minWidth: 100, maxWidth: 110)
                VStack {
                    Divider()
                        .padding(.leading, 10)
                        .padding(.trailing, 30)
                }
            }
        }
    }
}

#Preview {
    HomeCardDivider(tunnelStatus: TunnelStatus.loading)
}
