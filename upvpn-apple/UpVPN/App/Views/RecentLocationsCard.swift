//
//  RecentLocationsCard.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import SwiftUI

struct RecentLocationsCard: View {

    @EnvironmentObject private var locationViewModel: LocationViewModel

    var body: some View {
        // todo: fix: when recent locations are loading ui shows empty list
        VStack {
            List(selection: locationViewModel.selectionBinding) {
                Section("Recent Locations") {
                    // Last item in list is the most recent, hence show it on top by using reversed()
                    ForEach(locationViewModel.recentLocations.reversed()) { location in
                        LocationView(location: location)
                            .tag(location)
                    }
                }
            }
            .id(locationViewModel.locationsLastUpdated)
        }
    }
}

#Preview {
    RecentLocationsCard()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return false}))
        .environmentObject(TunnelViewModel())
}
