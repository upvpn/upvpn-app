//
//  ContentView.swift
//  UpVPN
//
//  Created by Himanshu on 6/20/24.
//

import SwiftUI

import NetworkExtension


struct ContentView: View {
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

    var body: some View {
        VStack {
            switch authViewModel.signInState {
            case .checkingLocal:
                WelcomeView()
            case .notSignedIn, .signingIn:
                AuthView()
            case .signedIn, .signingOut:
                MainView()
            }

        }
        // app could be launched when tunnel was started from system settings
        // hence update selected location from tunnelViewModel to locationViewModel
        .onReceive(tunnelViewModel.tunnelObserver.$tunnelStatus) { tunnelStatus in
            Task { @MainActor in
                if let location = tunnelStatus.currentLocation() {
                    locationViewModel.selected = location
                }
            }
        }
        .onReceive(authViewModel.$authDevice) { device in
            if let device = device {
                // todo: make dns configurable
                tunnelViewModel.tunnelConfig(device: device, dns: "1.1.1.1")
            }
        }
        .onAppear {
            tunnelViewModel.reload()
            locationViewModel.reload()
            locationViewModel.reloadRecent()
        }
    }
}


#Preview {
    ContentView()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
}
