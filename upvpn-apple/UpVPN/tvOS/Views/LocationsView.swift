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

    private let columns: [GridItem] = Array(repeating: .init(.flexible(), spacing: 55), count: 3)

    private var filteredLocations: [Location] {
        var computedLocations =  if search.isEmpty {
            locationViewModel.locations
        } else {
            locationViewModel.locations.filter { location in location.matches(query: search) }
        }

        computedLocations.sort { $0.city < $1.city }

        return computedLocations
    }

    var body: some View {
        ScrollView(.vertical) {
            LazyVGrid(columns: columns, spacing: 55) {
                ForEach(filteredLocations) { location in
                    LocationView(location: location)
                        .tag(location)
                }
            }
            .id(locationViewModel.locationsLastUpdated)

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
