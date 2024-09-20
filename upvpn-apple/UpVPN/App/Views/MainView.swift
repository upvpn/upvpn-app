//
//  HomeView.swift
//  UpVPN
//
//  Created by Himanshu on 7/26/24.
//

import SwiftUI

struct MainView: View {

    @EnvironmentObject private var locationViewModel: LocationViewModel
    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel

    @State private var isLocationErrorPresented = false
    @State private var isTunnelErrorPresented = false
    @State private var isSignOutErrorPresented = false

    @State private var isPlanManagmentPresented = false

    var body: some View {
        MainViewContent()
        .onReceive(locationViewModel.$locationError) { error in
            if let error = error, !error.isEmpty {
                isLocationErrorPresented = true
            }
        }
        .onReceive(tunnelViewModel.tunnelObserver.$lastError) { error in
            if error != nil {
                // print("tunnel error: \(error)")
                isTunnelErrorPresented = true
            }
        }
        .onReceive(authViewModel.$signOutErrorMessage) { error in
            if error != nil {
                // print("signout error: \(error)")
                isSignOutErrorPresented = true
            }
        }
        .alert(
            "Location",
            isPresented: $isLocationErrorPresented,
            presenting: locationViewModel.locationError
        ) { _ in
            Button(role: .cancel) {
                locationViewModel.clearError()
            } label: {
                Text("OK")
            }
        } message: { message in
            Text(message)
        }
        .alert(
            "VPN Tunnel",
            isPresented: $isTunnelErrorPresented,
            presenting: tunnelViewModel.tunnelObserver.lastError
        ) { error in
            Button(role: .cancel) {
                tunnelViewModel.clearLastError()
                if error.lowercased().contains("insufficient balance") {
                    isPlanManagmentPresented = true
                }

                if error == "unauthorized" {
                    authViewModel.signOut()
                }

            } label: {
                Text("OK")
            }
        } message: { error in
            Text(error)
        }
        .alert(
            "Sign Out",
            isPresented: $isSignOutErrorPresented,
            presenting: authViewModel.signOutErrorMessage
        ) { _ in
            Button(role: .cancel) {
                authViewModel.clearSignOutError()
            } label: {
                Text("OK")
            }
        } message: { message in
            Text(message)
        }
        .sheet(isPresented: $isPlanManagmentPresented) {
            if #available(iOS 16, macOS 13, *) {
                PlanManagement()
                    .presentationDragIndicator(.visible)
                    .presentationDetents([.fraction(0.2), .medium, .large])

            } else {
                PlanManagement()
                    #if os(macOS)
                    .toolbar {
                        // on macOS 12 ESC doesnt close it, hence provide a button
                        if #unavailable(macOS 13) {
                            Button {
                                isPlanManagmentPresented.toggle()
                            } label : {
                                Text("Close")
                            }
                        }
                    }
                    #endif
            }
        }
    }
}

#Preview {
    MainView()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true }))
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
}

