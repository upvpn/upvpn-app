//
//  HomeCard.swift
//  UpVPN
//
//  Created by Himanshu on 9/14/24.
//

import SwiftUI

struct HomeCard: View {
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel

    var body: some View {
        VStack(alignment: .leading) {
            Spacer()

            HStack {
                HStack(spacing: 15) {

                    Image(systemName: tunnelViewModel.tunnelObserver.tunnelStatus.shieldSystemImage())
                        .resizable()
                        .scaledToFit()
                        .frame(minWidth: 50, maxWidth: 100,  minHeight: 50, maxHeight: 100)
                        .font(.headline.weight(.light))

                    if case .connected(_, let date) = tunnelViewModel.tunnelObserver.tunnelStatus {
                        ElapsedTimeView(startDate: date)
                            .background(
                                Capsule()
                                    .fill(.ultraThinMaterial)
                                    .stroke(.white)
                            )
                    } else {
                        Text(tunnelViewModel.tunnelObserver.tunnelStatus.displayText())
                            .font(.headline)
                            .padding(.bottom, 2).padding(.top, 2)
                            .padding(.trailing, 10)
                            .padding(.leading, 10)
                            .background(
                                Capsule()
                                    .fill(.ultraThinMaterial)
                                    .stroke(.white)
                            )
                    }
                }
                .padding()

                Spacer()
                if tunnelViewModel.tunnelObserver.tunnelStatus.isConnected(),
                    let runtimeConfig = tunnelViewModel.tunnelObserver.runtimeConfig,
                   let peer = runtimeConfig.peers.first,
                   let tx = peer.txBytes,
                   let rx  = peer.rxBytes

                {

                    StatsCard(tx: prettyBytes(tx), rx: prettyBytes(rx))
                        .frame(maxHeight: .infinity)
                            .scaledToFit()
                            .padding()
                            .background(.ultraThinMaterial)
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                            .padding(.trailing, 10)

                }
            }
            .padding(.horizontal)

            Spacer()
           

        }
        .padding(.vertical, 10)
        .background(Color.clear)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .frame( maxWidth: .infinity)
    }
}

