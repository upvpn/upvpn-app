//
//  HomeCardLocation.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import SwiftUI

struct HomeCardLocation: View {
    /// The selected location (that of the session in progress, if any) as the API lists it,
    /// for an up to date estimate; from local information when the API no longer lists it,
    /// e.g. because it was disabled on the backend while connected to it. nil until there is
    /// a location to select.
    private var selectedLocation: Location? {
        guard let selected = self.locationViewModel.selected else { return nil }
        return self.locationViewModel.locations.first(where: { $0 == selected }) ?? selected
    }
    var isDisconnectedOrConnected: Bool = false
    var isDisconnected: Bool = true

    @EnvironmentObject var locationViewModel: LocationViewModel

    var body: some View {
        HStack(spacing: 15) {
            if let selectedLocation = selectedLocation {
                FlagImage(countryCode: selectedLocation.countryCode)
                Text(selectedLocation.displayText())
                    .font(.headline)

                if isDisconnectedOrConnected {
                    Circle()
                        .fill(selectedLocation.warmOrColdColor())
                        .frame(width: 12, height: 12)
                        .id(locationViewModel.locationsLastUpdated)
                } else {
                    ProgressView()
                        .modifier(ScaleEffectModifier())
                }
            } else {
                // no location yet (list not loaded or empty)
                Text("Select a location")
                    .font(.headline)
            }
        }
        .padding()
        .cornerRadius(15)
        .frame(maxWidth: .infinity)
        .contentShape(Rectangle())
        .contextMenu {
            ForEach(Location.countries(from: locationViewModel.locations)) { country in
                Section(country.name) {
                    ForEach(country.locations) { location in
                        Button {
                            locationViewModel.setSelected(location: location)
                        } label: {
                            LocationView(location: location)
                                .tag(location)
                        }
                        .disabled(!isDisconnected)
                    }
                }
            }
        }
    }
}

struct ScaleEffectModifier : ViewModifier {
    // scaleEffect crashes on macOS 12 hence this modifer
    func body(content: Content) -> some View {
        if #available(macOS 14, *) {
            content
            #if os(macOS)
                .scaleEffect(0.55)
            #endif
        } else {
            content
        }
    }
}


#Preview {
    HomeCardLocation()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
