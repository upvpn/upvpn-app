//
//  HomeCard.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import SwiftUI

struct HomeCard: View {
    var tunnelStatus: TunnelStatus
    var selectedLocation: Location?
    var start: () -> Void = {}
    var stop: () -> Void = {}

    private var isOnBinding: Binding<Bool> {
            Binding(
                get: { self.tunnelStatus.shouldToggleBeOn() },
                set: { newValue in
                    if newValue != self.tunnelStatus.shouldToggleBeOn() {
                        if newValue {
                            self.start()
                        } else {
                            self.stop()
                        }
                    }
                }
            )
        }


    var body: some View {
        VStack {
            Spacer()
            VStack(spacing: 15) {

                Image(systemName: tunnelStatus.shieldSystemImage())
                    .resizable()
                    .scaledToFit()
                    .frame(minWidth: 50, maxWidth: 100,  minHeight: 50, maxHeight: 100)
                    .font(.headline.weight(.light))

                if !tunnelStatus.isConnected() {
                    Text(tunnelStatus.displayText())
                        .font(.headline)
                        .padding(.bottom, 2).padding(.top, 2)
                        .padding(.trailing, 10)
                        .padding(.leading, 10)
                        .background(
                            Capsule().stroke()
                        )
                }
            }

            //            Spacer()
            HomeCardDivider(tunnelStatus: tunnelStatus)
                .padding(.vertical)
            //            Spacer()

            VStack(spacing: 15) {

                HomeCardLocation(
                    selectedLocation: selectedLocation ?? Location.default,
                    isDisconnectedOrConnected: tunnelStatus.isDisconnectedOrConnected(),
                    isDisconnected: tunnelStatus.isDisconnected())

                Toggle("", isOn: isOnBinding)
                    .labelsHidden()
                    .toggleStyle(.switch)
                    .disabled(!tunnelStatus.isDisconnectedOrConnected())
            }
            Spacer()
        }
        .padding(.vertical, 10)
        .background(Color.uSecondarySystemGroupedBackground)
        .cornerRadius(10)
        .frame(maxWidth: .infinity)
    }
}

#Preview {
    HomeCard(tunnelStatus: TunnelStatus.connected(Location.default, Date.now), selectedLocation: Location.default)
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}

#Preview {
    HomeCard(tunnelStatus: TunnelStatus.serverRunning(Location.default), selectedLocation: Location.default)
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
