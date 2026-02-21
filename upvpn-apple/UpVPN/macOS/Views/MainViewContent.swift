//
//  MainViewContent.swift
//  UpVPNmacOS
//
//  Created by Himanshu on 8/15/24.
//

import SwiftUI

@available(macOS, introduced: 12, obsoleted: 13)
struct MainViewContent12: View {

    @State private var showInspector: Bool = false
    @State private var showSettings: Bool = false
    @State private var showPlans: Bool = false

    var body: some View {
        NavigationView {
            LocationsView()
                .frame(minWidth: 325)

            HStack {
                if showInspector {
                    HomeView(onStatsTap: { showInspector.toggle() })
                        .frame(minWidth: 400, maxWidth: .infinity)
                    RuntimeConfigurationView()
                        .frame(minWidth: 400)
                } else {
                    HomeView(onStatsTap: { showInspector.toggle() })
                        .frame(minWidth: 400, maxWidth: .infinity)
                }
            }
            .toolbar {
                Button {
                    showSettings.toggle()
                } label: {
                    Label("Account", systemImage: "person")
                }

                Button {
                    showInspector.toggle()
                } label: {
                    Label("Configuration", systemImage: "sidebar.right")
                }
            }
            .sheet(isPresented: $showSettings) {

                VStack(spacing: 20) {
                    AccountView()

                    Button("Plan") {
                        showSettings.toggle()
                        showPlans.toggle()
                    }

                    SignOutView()

                    VersionView()
                }
                .padding()
                .buttonStyle(LinkButtonStyle())

                .toolbar {
                    Button {
                        showSettings.toggle()
                    } label: {
                        Text("Close")
                    }
                }
            }
            .sheet(isPresented: $showPlans) {
                PlanManagement()
                    .toolbar {
                        Button {
                            showPlans.toggle()
                        } label: {
                            Text("Close")
                        }
                    }
            }
        }
    }
}

@available(macOS 26, *)
struct MainViewContent26: View {
    @State private var showInspector: Bool = false
    @State private var showAccountSheet: Bool = false

    var body: some View {
        NavigationSplitView {
            LocationsView()
                .frame(maxWidth: .infinity)
                .navigationSplitViewColumnWidth(
                    min: 310,
                    ideal: 325,
                    max: .infinity
                )
        } detail: {
            HomeView(onStatsTap: { showInspector.toggle() })
                .navigationSplitViewColumnWidth(min: 400, ideal: 400, max: 400)
                .toolbar {
                    ToolbarView26(
                        showInspector: $showInspector,
                        showAccountSheet: $showAccountSheet
                    )
                }
        }
        .navigationSplitViewStyle(.balanced)
        .inspector(isPresented: $showInspector) {
            RuntimeConfigurationView()
                .inspectorColumnWidth(min: 400, ideal: 400, max: 400)

        }
        .sheet(isPresented: $showAccountSheet) {
            NavigationStack {
                SettingsView()
                    .toolbar {
                        Button(action: { showAccountSheet.toggle() }) {
                            Label("Close", systemImage: "xmark.circle")
                        }
                    }
            }
        }
    }
}

@available(macOS 26, *)
struct ToolbarView26: ToolbarContent {
    @Binding var showInspector: Bool
    @Binding var showAccountSheet: Bool

    var body: some ToolbarContent {
        ToolbarItem {
            Button(action: { showAccountSheet.toggle() }) {
                Label("Account", systemImage: "person")
            }
            .help("Account")
        }

        ToolbarItem {
            Button(action: { showInspector.toggle() }) {
                Label("WireGuard Configuration", systemImage: "sidebar.right")
            }
            .help("WireGuard Configuration")
        }
    }
}

@available(macOS 14, *)
struct MainViewContent14: View {
    @State private var showInspector: Bool = false

    var body: some View {
        // NavigationStack & NavigationSplitView available from macOS 13+
        NavigationStack {
            NavigationSplitView {
                LocationsView()
                    .frame(maxWidth: .infinity)
                    .navigationSplitViewColumnWidth(
                        min: 310,
                        ideal: 325,
                        max: .infinity
                    )
            } detail: {
                HomeView(onStatsTap: { showInspector.toggle() })
                    .navigationSplitViewColumnWidth(
                        min: 400,
                        ideal: 400,
                        max: .infinity
                    )
            }
            .navigationSplitViewStyle(.balanced)
            .toolbar {
                ToolbarView(showInspector: $showInspector)
            }
            // .inspector available only in macOS14+
            .inspector(isPresented: $showInspector) {
                RuntimeConfigurationView()
                    .frame(minWidth: 300)
                    .inspectorColumnWidth(min: 400, ideal: 500, max: 600)
            }
        }
    }
}

@available(macOS, introduced: 13, obsoleted: 14)
struct MainViewContent13: View {

    @State private var showInspector: Bool = false

    var body: some View {
        NavigationStack {
            NavigationSplitView {
                LocationsView()
                    .frame(maxWidth: .infinity)
                    .navigationSplitViewColumnWidth(
                        min: 310,
                        ideal: 325,
                        max: .infinity
                    )
            } detail: {
                if showInspector {
                    HStack {
                        HomeView(onStatsTap: { showInspector.toggle() })
                            .frame(minWidth: 400)

                        RuntimeConfigurationView()
                            .frame(minWidth: 400)
                    }
                    .navigationSplitViewColumnWidth(
                        min: 700,
                        ideal: 800,
                        max: .infinity
                    )
                } else {
                    HomeView(onStatsTap: { showInspector.toggle() })
                        .navigationSplitViewColumnWidth(
                            min: 400,
                            ideal: 400,
                            max: .infinity
                        )
                }
            }

            .navigationSplitViewStyle(.balanced)
            .toolbar {
                ToolbarView(showInspector: $showInspector)
            }
        }
    }
}

struct ToolbarView: View {
    @Binding var showInspector: Bool

    var body: some View {
        NavigationLink {
            SettingsView()
                .frame(minWidth: 400, maxWidth: .infinity)
        } label: {
            Label("Account", systemImage: "person")
        }
        .help("Account")

        Button(action: { showInspector.toggle() }) {
            Label("Configuration", systemImage: "sidebar.right")
        }
        .help("WireGuard Config")
    }
}

struct MainViewContent: View {
    var body: some View {
        if #available(macOS 26, *) {
            MainViewContent26()
        } else if #available(macOS 14, *) {
            MainViewContent14()
        } else if #available(macOS 13, *) {
            MainViewContent13()
        } else {
            MainViewContent12()
        }
    }
}

#Preview {
    MainViewContent()
}
