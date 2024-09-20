//
//  LocationsView.swift
//  UpVPN
//
//  Created by Himanshu on 7/27/24.
//

import SwiftUI

struct LocationsView: View {

    @EnvironmentObject private var locationViewModel: LocationViewModel

    @State private var search: String = ""

    var showMapInToolbar: Bool = false

    private var filteredLocations: [Location] {
        return if search.isEmpty {
            locationViewModel.locations
        } else {
            locationViewModel.locations.filter { location in location.matches(query: search) }
        }
    }

    var body: some View {
        List(selection: locationViewModel.selectionBinding) {
            ForEach(Location.countries(from: filteredLocations)) { country in
                Section(country.name) {
                    ForEach(country.locations) { location in
                        LocationView(location: location)
                            .tag(location)
                    }
                }
            }
        }
        .id(locationViewModel.locationsLastUpdated)
        .searchable(text: $search, placement: .sidebar)
        .refreshable {
            locationViewModel.reload()
        }
        .onAppear {
            locationViewModel.reload()
        }
        .toolbar {
            if showMapInToolbar {
                #if os(iOS)
                if #available(iOS 17, *) {
                    NavigationLink {
                        LocationsMapView()
                            .navigationTitle("Locations Map")
                            .navigationBarTitleDisplayMode(.inline)
                            .toolbarBackground(.hidden, for: .automatic)
                    } label: {
                        Label("Map", systemImage: "map")
                    }
                }
                #endif
            }
        }
    }
}

#Preview {
    LocationsView()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
