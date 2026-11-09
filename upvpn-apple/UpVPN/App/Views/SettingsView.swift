//
//  SettingsView.swift
//  UpVPN
//
//  Created by Himanshu on 7/27/24.
//

import SwiftUI

struct SettingsView: View {
    /// When set, the Plan row runs this instead of pushing the plan view. The macOS 26
    /// account sheet uses it to open the plan in a sheet of its own: a StoreKit purchase
    /// made from a view pushed inside the account sheet attaches StoreKit's confirmation
    /// sheet to that window and leaves it with a taller title bar afterwards.
    var onPlan: (() -> Void)? = nil

    var body: some View {
        Form {
            Section("Profile") {
                AccountView()

                if let onPlan = onPlan {
                    // drawn like the NavigationLink rows around it
                    Button(action: onPlan) {
                        HStack {
                            Text("Plan")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.footnote.weight(.semibold))
                                .foregroundStyle(.tertiary)
                        }
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                } else {
                    NavigationLink("Plan") {
                        PlanManagement(isRefreshable: true)
                    }
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
