//
//  HomeView.swift
//  UpVPNtvOS
//
//  Created by Himanshu on 9/14/24.
//

import SwiftUI

private func showProgressView(_ tunnelStatus: TunnelStatus) -> Bool {
    switch tunnelStatus {
    case .requesting, .accepted, .serverCreated, .serverRunning, .serverReady, .connecting:
        return true
    default:
        return false
    }
}

struct HomeView: View {
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

    var body: some View {
        ScrollView(.vertical) {
            VStack(alignment: .leading) {
                // header
                ZStack {
                    LocationsMapView(coordinateSpan: .large)
                        .focusable(false)
                        .clipShape(RoundedRectangle(cornerRadius: 20))

                    VStack {
                        HomeCard()
                    }

                    if showProgressView(tunnelViewModel.tunnelObserver.tunnelStatus) {
                        VStack {
                            Spacer()
                            ProgressView(value: tunnelViewModel.tunnelObserver.tunnelStatus.progress())
                                .tint(.blue)
                                .padding(.horizontal, 30)
                                .padding(.bottom, 30)
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .containerRelativeFrame(.vertical, alignment: .topLeading) {
                    length, _ in length * 0.5
                }
                .focusable(false)

                VPNSwitch(tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                          start: {
                    if let location = locationViewModel.selected {
                        locationViewModel.addRecent(location: location)
                        tunnelViewModel.start(to: location)
                    }
                },
                          stop: {
                    tunnelViewModel.stop()
                })
                .scrollClipDisabled()
                .padding(.bottom, 25)

                if !locationViewModel.recentLocations.isEmpty {
                    Section("Recent Locations") {
                        ScrollView(.horizontal) {
                            LazyHStack(spacing: 30) {
                                ForEach(locationViewModel.recentLocations.reversed()) { location in
                                    LocationView(location: location)
                                        .padding(.top, 10)
                                        .padding(.bottom, 45)
                                }
                            }
                            .id(locationViewModel.locationsLastUpdated)
                        }
                    }
                }

                ForEach(Location.countries(from: locationViewModel.locations)) { country in
                    Section(country.name) {
                        ScrollView(.horizontal) {
                            LazyHStack(spacing: 30) {
                                ForEach(country.locations) { location in
                                    LocationView(location: location)
                                        .padding(.top, 10)
                                        .padding(.bottom, 45)
                                }
                            }
                            .id(locationViewModel.locationsLastUpdated)
                        }
                    }
                }

            }
            .scrollTargetLayout()
        }
        .scrollClipDisabled()
    }
}

#Preview {
    let locationViewModel = LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true})
    //locationViewModel.recentLocations = [Location.default, Location.testLocation]
    return HomeView()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
        .environmentObject(locationViewModel)
}
