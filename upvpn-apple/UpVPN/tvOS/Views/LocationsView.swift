//
//  LocationsView.swift
//  UpVPN
//
//  Created by Himanshu on 9/15/24.
//

import SwiftUI

struct LocationsView: View {

    @EnvironmentObject private var locationViewModel: LocationViewModel

    @State private var search: String = ""
    @State private var tierFilter: LocationTierFilter = .all

    private let columns: [GridItem] = Array(repeating: .init(.flexible(), spacing: 55), count: 3)

    private var tiers: [LocationTier] {
        locationViewModel.locations.availableTiers
    }

    private var filteredLocations: [Location] {
        var computedLocations = locationViewModel.locations.filter { location in
            tierFilter.matches(location) && (search.isEmpty || location.matches(query: search))
        }

        computedLocations.sort { $0.city < $1.city }

        return computedLocations
    }

    var body: some View {
        ScrollView(.vertical) {
            VStack(spacing: 40) {
                // a tier is worth filtering by only when there is more than one
                if tiers.count > 1 {
                    Picker("Tier", selection: $tierFilter) {
                        Text(LocationTierFilter.all.label)
                            .tag(LocationTierFilter.all)
                        ForEach(tiers, id: \.self) { tier in
                            Text(tier.displayName)
                                .tag(LocationTierFilter.tier(tier))
                        }
                    }
                    .pickerStyle(.segmented)
                    .labelsHidden()
                    .frame(maxWidth: 900)
                }

                LazyVGrid(columns: columns, spacing: 55) {
                    ForEach(filteredLocations) { location in
                        LocationView(location: location)
                            .tag(location)
                    }
                }
                .id(locationViewModel.locationsLastUpdated)
            }
        }
        .onChange(of: tiers) { tiers in
            // the filtered tier is gone from the list (e.g. free locations no longer offered)
            if case .tier(let tier) = tierFilter, !tiers.contains(tier) {
                tierFilter = .all
            }
        }
        .scrollClipDisabled()
        .searchable(text: $search, placement: .automatic)
        .refreshable {
            locationViewModel.reload()
        }
        .onAppear {
            locationViewModel.reload()
        }
    }
}

#Preview {
    LocationsView()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
