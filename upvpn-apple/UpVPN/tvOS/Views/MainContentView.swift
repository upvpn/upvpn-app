//
//  MainContentView.swift
//  UpVPNmacOS
//
//  Created by Himanshu on 9/13/24.
//


import SwiftUI

struct MainViewContent : View {

    @EnvironmentObject var tunnelViewModel: TunnelViewModel

    var body: some View {
        NavigationStack {
            TabView() {
                Tab("Home", systemImage: "house") {
                    HomeView()
                }

                Tab("Locations", systemImage: "location") {
                    LocationsView()
                }

                Tab("Config", systemImage: "network") {
                    RuntimeConfigurationView()
                }

                Tab("Account", systemImage: "person") {
                    SettingsView()
                }
            }
        }
    }
}

#Preview {
    let locationViewModel = LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true})

    return MainViewContent()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
        .environmentObject(locationViewModel)
}
