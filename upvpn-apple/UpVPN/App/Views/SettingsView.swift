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
