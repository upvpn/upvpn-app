//
//  SettingsView.swift
//  UpVPN
//
//  Created by Himanshu on 7/27/24.
//

import SwiftUI

struct SettingsView: View {

    var body: some View {
        Form {
            Section("Profile") {
                AccountView()

                NavigationLink("Plan") {
                    PlanManagement(isRefreshable: true)
                }

                NavigationLink("Help") {
                    HelpView()
                }

                SignOutView()
            }
            #if !os(tvOS)
            if #available(iOS 16, macOS 13, *) {
                Section("Referrals") {
                    ShareLink(
                        item: URL(string: "https://UpVPN.app")!,
                        message: Text("Check out this cool VPN app! https://UpVPN.app")
                    ) {
                        Label("Refer a friend", systemImage: "gift.fill")
                    }
                }
            }
            #endif

            Section("Version") {
               VersionView()
            }
        }
        .modifier(FormModifier())
    }
}

struct FormModifier: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 16, macOS 13.0, *) {
            content.formStyle(.grouped)
        } else {
            content
        }
    }
}

#Preview {
    SettingsView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
