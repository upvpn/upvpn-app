//
//  HomeView.swift
//  UpVPN
//
//  Created by Himanshu on 7/27/24.
//

import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

    var onStatsTap: () -> Void = {}

    var body: some View {
        GeometryReader { reader in
            VStack(spacing: 0) {
                ZStack {

                    Rectangle()
                        .fill(Color.uSystemGroupedBackground)
                        .ignoresSafeArea()
                    HomeCard(tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                             selectedLocation: locationViewModel.selected,
                             start: {
                        if let location = locationViewModel.selected {
                            locationViewModel.addRecent(location: location)
                            tunnelViewModel.start(to: location)
                        }
                    },
                             stop: {
                        tunnelViewModel.stop()
                    })
                    #if os(iOS)
                    .padding(.horizontal)
                    // to match padding as much as the card below it
                    .padding(.horizontal, 3)
                    // to keep distance from status bar at top
                    .padding(.top, 5)
                    #endif
                    #if os(macOS)
                    .padding([.leading, .top, .trailing])
                    #endif

                }
                .frame(minHeight: reader.size.height * 0.55)

                if tunnelViewModel.tunnelObserver.tunnelStatus.isConnected(),
                    let runtimeConfig = tunnelViewModel.tunnelObserver.runtimeConfig,
                   let peer = runtimeConfig.peers.first,
                   let tx = peer.txBytes,
                   let rx  = peer.rxBytes

                {
                    CardContainer {
                        StatsCard(tx: prettyBytes(tx), rx: prettyBytes(rx))


                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        onStatsTap()
                    }
                    //                    CardContainer {
                    //                        if let runtimeConfig = tunnelViewModel.tunnelObserver.runtimeConfig {
                    //                            if let peer = runtimeConfig.peers.first {
                    //                                if let tx = peer.txBytes, let rx  = peer.rxBytes {
                    //                                    StatsCard(tx: prettyBytes(tx), rx: prettyBytes(rx))
                    //                                }
                    //                            }
                    //                        }
                    //                    }
                } else {
                    if locationViewModel.recentLocations.isEmpty {
                        CardContainer {
                            WelcomeView(showSpinnner: false)
                        }
                    } else {
                        RecentLocationsCard()
                        #if os(macOS)
                            .cornerRadius(10)
                            .padding()
                        #endif
                    }
                }
            }
        }
        .onAppear {
            // for macOS locations are reloaded by LocationsView
            #if os(iOS)
            locationViewModel.reload()
            #endif
        }
    }
}


/* Bare bones home view for development
struct HomeViewBackup: View {
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

    var body: some View {
        VStack {
            ScrollView {
                Image(systemName: "shield")
                    .imageScale(.large)
                    .foregroundStyle(.tint)
                Text("Welcome to UpVPN!")

                Picker("Location", selection: $locationViewModel.selected) {
                    ForEach(locationViewModel.locations) { location in
                        Text(location.city).tag(location as Location?)
                    }
                }.disabled(locationViewModel.loading)

                if locationViewModel.locations.isEmpty {
                    Button("Reload Locations") {
                        locationViewModel.reload()
                    }.disabled(locationViewModel.loading)
                }

                if let selected = locationViewModel.selected {
                    Text("Selected: \(selected.city)")
                }

                if locationViewModel.recentLocations.isEmpty {
                    Text("No Recent")
                } else {
                    Text("Recent Locations:")
                    ForEach(locationViewModel.recentLocations.reversed()) { location in
                        Text(location.city)
                    }
                }

                Button {
                    let location = locationViewModel.selected ?? Location.default
                    locationViewModel.addRecent(location: location)
                    tunnelViewModel.start(to: location)
                } label: {
                    Text("Start")
                }.disabled(!tunnelViewModel.tunnelObserver.tunnelStatus.isDisconnected())
                Button {
                    tunnelViewModel.stop()
                } label: {
                    Text("Stop")
                }//.disabled(!tunnelViewModel.tunnelObserver.tunnelStatus.isConnected())

                Button {
                    print(tunnelViewModel.tunnelObserver.tunnelStatus)
                } label: {
                    Text("Print Status")
                }

                Button("Sign Out") {
                    print("signing out ...")
                    authViewModel.signOut()
                }.disabled(authViewModel.signInState == .signingOut)

                if let signOutErrorMessage = authViewModel.signOutErrorMessage {
                    Text("sign out error: \(signOutErrorMessage)")
                }

                Text("status: \(tunnelViewModel.tunnelObserver.tunnelStatus)")


                if let runtimeConfig = tunnelViewModel.tunnelObserver.runtimeConfig {
                    ForEach(runtimeConfig.peers,  id: \.publicKey) { peer in
                        if let endpoint = peer.endpoint {
                            Text("endpoint: \(endpoint)")
                        }
                        if let tx = peer.txBytes, let rx  = peer.rxBytes {
                            Text("tx: \(prettyBytes(tx)) rx: \(prettyBytes(rx))")
                        }
                    }
                }

                if let lastError = tunnelViewModel.tunnelObserver.lastError {
                    Text("last error: \(lastError)")
                        .onTapGesture {
                            tunnelViewModel.clearLastError()
                        }
                }
            }
        }
    }
}
*/


#Preview {
    let locationViewModel = LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true})
    //locationViewModel.recentLocations = [Location.default, Location.testLocation]
    return HomeView()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
        .environmentObject(locationViewModel)
}

