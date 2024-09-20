//
//  MainViewContent.swift
//  UpVPNiOS
//
//  Created by Himanshu on 8/15/24.
//

import SwiftUI

struct MainViewContentOld: View {
    @State private var selectedTab = 2

    @State private var showInspector = false

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationView {
                LocationsView()
                    .navigationTitle("Locations")
            }
            // for iOS 15 (otherwise it crashes):
            // https://stackoverflow.com/questions/65316497/swiftui-navigationview-navigationbartitle-layoutconstraints-issue
            .navigationViewStyle(.stack) // only available on ios
            .tabItem {
                Label("Locations", systemImage: "location")
            }.tag(1)

            HomeView(onStatsTap: { showInspector.toggle() })
                .modifier(InspectorModifier(showInspector: $showInspector))
                .tabItem {
                    Label("Home", systemImage: "house")
                }.tag(2)

            NavigationView {
                SettingsView()
                    .navigationTitle("Account")
            }
            // for iOS 15 (otherwise it crashes):
            // https://stackoverflow.com/questions/65316497/swiftui-navigationview-navigationbartitle-layoutconstraints-issue
            .navigationViewStyle(.stack) // only available on ios
            .tabItem {
                Label { Text("Account") } icon: {
                    Image(systemName:"person")
                        .overlay(Image(systemName: "gearshape"))
                }
            }.tag(3)
        }
    }
}

@available(iOS 16, *)
struct MainViewContentNew: View {
    @State private var selectedTab = 2

    @State private var showInspector = false

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                LocationsView()
                    .navigationTitle("Locations")
            }
            .tabItem {
                Label("Locations", systemImage: "location")
            }.tag(1)

            HomeView(onStatsTap: { showInspector.toggle() })
                .modifier(InspectorModifier(showInspector: $showInspector))
                .tabItem {
                Label("Home", systemImage: "house")
            }.tag(2)

            NavigationStack {
                SettingsView()
                    .navigationTitle("Account")
            }
            .tabItem {
                Label { Text("Account") } icon: {
                    Image(systemName:"person")
                        .overlay(Image(systemName: "gearshape"))
                }
            }.tag(3)
        }
    }
}

struct InspectorModifier: ViewModifier {
    @Binding var showInspector: Bool

    func body(content: Content) -> some View {
        if #available(iOS 17, *) {
            content
                .inspector(isPresented: $showInspector) {
                    RuntimeConfigurationView()
                }
        } else {
            content
                .sheet(isPresented: $showInspector) {
                    RuntimeConfigurationView()
                }
        }
    }
}

struct MainViewContent : View {
    var body: some View {
        if #available(iOS 16, *) {
            MainViewContentNew()
        } else {
            MainViewContentOld()
        }
    }
}


#Preview {
    MainViewContent()
}
