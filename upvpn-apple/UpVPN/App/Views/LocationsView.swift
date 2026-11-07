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
    @State private var tierFilter: LocationTierFilter = .all

    var showMapInToolbar: Bool = false

    private var tiers: [LocationTier] {
        locationViewModel.locations.availableTiers
    }

    private var filteredLocations: [Location] {
        locationViewModel.locations.filter { location in
            tierFilter.matches(location) && (search.isEmpty || location.matches(query: search))
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
        .onChange(of: tiers) { tiers in
            // the filtered tier is gone from the list (e.g. free locations no longer offered)
            if case .tier(let tier) = tierFilter, !tiers.contains(tier) {
                tierFilter = .all
            }
        }
        .refreshable {
            locationViewModel.reload()
        }
        .onAppear {
            locationViewModel.reload()
        }
        .toolbar {
            // a tier is worth filtering by only when there is more than one
            if tiers.count > 1 {
                TierFilterMenu(tiers: tiers, selection: $tierFilter)
            }
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

/// Native toolbar filter: a filter button that opens a checkmark list of "All" and the
/// tiers, as in Mail or Files. The icon is filled while a tier filter is active.
struct TierFilterMenu: View {
    var tiers: [LocationTier]
    @Binding var selection: LocationTierFilter

    var body: some View {
        Menu {
            Picker("Show", selection: $selection) {
                Text(LocationTierFilter.all.label)
                    .tag(LocationTierFilter.all)
                ForEach(tiers, id: \.self) { tier in
                    Text(tier.displayName)
                        .tag(LocationTierFilter.tier(tier))
                }
            }
            .pickerStyle(.inline)
        } label: {
            Label("Filter",
                  systemImage: selection == .all
                    ? "line.3.horizontal.decrease.circle"
                    : "line.3.horizontal.decrease.circle.fill")
        }
    }
}

#Preview {
    LocationsView()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
