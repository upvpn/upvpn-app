//
//  HomeCardLocation.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import SwiftUI

struct HomeCardLocation: View {
    var selectedLocation: Location = Location.default
    var isDisconnectedOrConnected: Bool = false
    var isDisconnected: Bool = true

    @EnvironmentObject var locationViewModel: LocationViewModel

    var body: some View {
        HStack(spacing: 15) {
            FlagImage(countryCode: selectedLocation.countryCode)
            Text(selectedLocation.displayText())
                .font(.headline)

            if isDisconnectedOrConnected {
                Circle()
                    .fill(selectedLocation.warmOrColdColor())
                    .frame(width: 12, height: 12)
            } else {
                ProgressView()
                    .modifier(ScaleEffectModifier())
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
