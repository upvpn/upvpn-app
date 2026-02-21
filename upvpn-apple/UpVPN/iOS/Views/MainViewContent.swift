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
                if #available(iOS 17, *), UIDevice.current.userInterfaceIdiom == .pad {
                    NavigationSplitView {
                        LocationsView(showMapInToolbar: UIDevice.current.userInterfaceIdiom == .phone)
                    } detail: {
                        LocationsMapView(
                            coordinateSpan: .large
                        )
                        // to remove transparent background bar on top
                        .toolbarBackground(.hidden, for: .automatic)
                    }
                    // hack: otherwise searchbox shows up on toolbar
                    .searchable(text: .constant(""), placement: .sidebar)
                    .navigationTitle("Locations")
                    // hack: otherwise an empty toolbar (and navigation title) with big height shows up.
                    .toolbar(.hidden)
                } else {
                    LocationsView(showMapInToolbar: UIDevice.current.userInterfaceIdiom == .phone)
                        .navigationTitle("Locations")
                }
            }
            .tabItem {
                Label("Locations", systemImage: "location")
            }.tag(1)

            ResponsiveHomeView(onStatsTap: { showInspector.toggle() })
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

@available(iOS 18, *)
struct MainViewContent18: View {
    @State private var selectedTab = 2

    @State private var showInspector = false

    var body: some View {
        TabView(selection: $selectedTab) {
            
            Tab("Locations", systemImage: "location", value: 1) {
                NavigationStack {
                    if UIDevice.current.userInterfaceIdiom == .pad {
                        NavigationSplitView {
                            LocationsView(showMapInToolbar: UIDevice.current.userInterfaceIdiom == .phone)
                        } detail: {
                            LocationsMapView(
                                coordinateSpan: .large
                            )
                            // to remove transparent background bar on top
                            .toolbarBackground(.hidden, for: .automatic)
                        }
                        // hack: otherwise searchbox shows up on toolbar
                        .searchable(text: .constant(""), placement: .sidebar)
                        .navigationTitle("Locations")
                        // hack: otherwise an empty toolbar (and navigation title) with big height shows up.
                        .toolbar(.hidden)
                    } else {
                        LocationsView(showMapInToolbar: UIDevice.current.userInterfaceIdiom == .phone)
                            .navigationTitle("Locations")
                    }
                }
                .environment(\.horizontalSizeClass, .regular)
            }

            Tab("Home", systemImage: "house", value: 2) {
                ResponsiveHomeView(onStatsTap: { showInspector.toggle() })
                    .modifier(InspectorModifier(showInspector: $showInspector))
                    .environment(\.horizontalSizeClass, .regular)
            }


            Tab(value: 3, content:{ NavigationStack {
                SettingsView()
                    .navigationTitle("Account")
            }
            .environment(\.horizontalSizeClass, .regular)
            }, label: {
                Label { Text("Account") } icon: {
                    Image(systemName:"person")
                        .overlay(Image(systemName: "gearshape"))
                }})

        }
        .tabViewStyle(.tabBarOnly)
        .environment(\.horizontalSizeClass, .compact)
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
        if #available(iOS 18, *) {
            MainViewContent18()
        } else if #available(iOS 16, *) {
            MainViewContentNew()
        } else {
            MainViewContentOld()
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
