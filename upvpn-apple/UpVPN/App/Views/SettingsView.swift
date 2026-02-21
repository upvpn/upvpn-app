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
            Section("Account") {
                AccountView()

                NavigationLink("Plan") {
                    PlanManagement(isRefreshable: true)
                }

                NavigationLink("Help") {
                    HelpView()
                }

            }
            #if !os(tvOS)
            if #available(iOS 16, macOS 13, *) {
                Section("Share") {
                    ShareLink(
                        item: URL(string: "https://UpVPN.app")!,
                        message: Text("Check out this cool VPN app! https://UpVPN.app")
                    ) {
                        Label("Share UpVPN", systemImage: "square.and.arrow.up")
                    }
                }
            }
            #endif

            Section("Version") {
               VersionView()
            }

            SignOutView()
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
