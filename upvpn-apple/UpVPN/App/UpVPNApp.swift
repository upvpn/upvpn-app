//
//  UpVPNApp.swift
//  UpVPN
//
//  Created by Himanshu on 6/20/24.
//

import SwiftUI
import NetworkExtension

@main
struct UpVPNApp: App {

    @StateObject var tunnelViewModel: TunnelViewModel
    @StateObject var authViewModel: AuthViewModel
    @StateObject var locationViewModel: LocationViewModel
    @StateObject var planViewModel: PlanViewModel = PlanViewModel()

    init() {
        let tunnelVM = TunnelViewModel()
        _tunnelViewModel = StateObject(wrappedValue: tunnelVM)
        _authViewModel = StateObject(wrappedValue: AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {
            return tunnelVM.tunnelObserver.tunnelStatus.isDisconnected()
        }))
        // To prevent tight coupling, and yet ability for locationVM to determine if its safe to set location, isDisconnected is provided as closure
        _locationViewModel = StateObject(wrappedValue: LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {
            return tunnelVM.tunnelObserver.tunnelStatus.isDisconnected()
        }))

        Task {
            // device will be initialized here or when a user signs in
            await DeviceStore.getOrInitializeDevice()
        }
    }


    private func getContentView() -> some View {
        return ContentView()
            .environmentObject(tunnelViewModel)
            .environmentObject(authViewModel)
            .environmentObject(locationViewModel)
            .environmentObject(planViewModel)
            #if os(macOS)
            .frame(minHeight: 650)
            #endif
    }


    var body: some Scene {

        windowScene()

        #if os(macOS)
        if #available(macOS 13, *) {
            MenuBarExtra {
                MenuBarExtraWrapper()
                    .onAppear {
                        // to load new locations and/or estimates
                        locationViewModel.reload()
                    }
                    .environmentObject(tunnelViewModel)
                    .environmentObject(authViewModel)
                    .environmentObject(locationViewModel)
                    .environmentObject(planViewModel)
            } label: {
                Image("upvpn_badge")
                    .renderingMode(.template)
            }
            .menuBarExtraStyle(.window)
        }
        #endif
    }


    func windowScene() -> some Scene {
        #if os(macOS)
        if #available(macOS 13, *) {
            return Window("UpVPN", id: "main") {
                getContentView()
            }

        } else {
            return WindowGroup {
                getContentView()
            }
        }
        #else
        return WindowGroup {
            getContentView()
        }
        #endif
    }


}

#if os(macOS)
struct MenuBarExtraWrapper: View {

    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

//    @Environment(\.openWindow) var openWindow

    var body: some View {
        switch authViewModel.signInState {
        case .signedIn:
            VStack(spacing: 10) {
                HomeCard(tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                         start: {
                    if let location = locationViewModel.selected {
                        locationViewModel.addRecent(location: location)
                        tunnelViewModel.start(to: location)
                    }
                },
                         stop: {
                    tunnelViewModel.stop()
                })
                .aspectRatio(1, contentMode: .fill)

                HStack {
                    Text(tunnelViewModel.tunnelObserver.tunnelStatus.isDisconnected() ? "Quit" : "Stop and Quit").onTapGesture {
                        Task {
                            tunnelViewModel.stop()
                            try? await Task.sleep(nanoseconds: 100_000_000)
                            NSApplication.shared.terminate(nil)
                        }
                    }
                }
                .font(.headline)
                .foregroundColor(.blue)
            }
            .aspectRatio(1, contentMode: .fit)
            .padding(10)
        case .signingIn:
            Text("Signing In ...")
                .padding()
        case .signingOut:
            Text("Signing Out ...")
                .padding()
        case .checkingLocal:
            Text("Loading ...")
                    .padding()
        case .notSignedIn:
            Text("Please sign in")
                .padding()
        }
    }

}
#endif
