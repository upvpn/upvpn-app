//
//  LocationViewModel.swift
//  UpVPN
//
//  Created by Himanshu on 7/24/24.
//

import Foundation
import SwiftUI

@MainActor
class LocationViewModel: ObservableObject {

    private static var cannotChangeWhenInProgress = "Cannot change location, VPN session is in progress"

    @Published var locations: [Location] = []
    // When location estimate changes we need a way to identify that
    // and update List view, hence last udpated serves as idenfitifier
    // for list views
    @Published var locationsLastUpdated: Date = Date()
    // most recent is in the end of the list
    @Published var recentLocations: [Location] = []
    @Published var loading: Bool = false
    @Published var selected: Location? = nil

    @Published var locationError: String?

    private var dataRepository: DataRepository
    private var isDisconnected: () -> Bool

    // For iOS 16+ List selection
    var selectionBinding: Binding<Location?> {
            Binding(
                get: { self.selected },
                set: { newValue in
                    if newValue != self.selected {
                        if self.isDisconnected() {
                            if let location = newValue {
                                self.selected = location
                            }
                        } else {
                            self.locationError = LocationViewModel.cannotChangeWhenInProgress
                        }
                    }
                }
            )
    }

    init(dataRepository: DataRepository, isDisconnected: @escaping () -> Bool) {
        self.dataRepository = dataRepository
        self.isDisconnected = isDisconnected
    }

    /// update estimates of recent locations & selected location
    private func updateEstimates() {
        for recentIdx in self.recentLocations.indices {
            if let foundIdx = self.locations.firstIndex(of: self.recentLocations[recentIdx]) {
                self.recentLocations[recentIdx].estimate = self.locations[foundIdx].estimate
            }
        }

        if let selected = self.selected {
            if let foundIdx = self.locations.firstIndex(of: selected) {
                self.selected?.estimate = self.locations[foundIdx].estimate
            }
        }
    }

    func reload() {
        self.loading = true
        Task {
            let stream = await DataRepository.shared.getLocations()

            for await locations in stream {
                await MainActor.run {
                    if !locations.isEmpty {
                        self.locations = locations
                        if !self.locations.isEmpty && self.selected == nil {
                            self.selected = self.locations.first { location in
                                location.city.lowercased() == "ashburn" } ?? self.locations[0]
                            self.loading = false
                        }

                        // upon fetching new locations
                        self.updateEstimates()
                        self.locationsLastUpdated = Date()
                    }
                }
            }
            self.loading = false
        }
    }

    func reloadRecent() {
        Task {
            let recentLocations = await self.dataRepository.loadRecent()
            await MainActor.run {
                self.recentLocations = recentLocations
            }
        }
    }

    func addRecent(location: Location) {
        if let index = self.recentLocations.firstIndex(of: location) {
            self.recentLocations.remove(at: index)
        }
        self.recentLocations.append(location)

        Task {
            await self.dataRepository.addRecent(location: location)
        }
    }

    func setSelected(location: Location) {
        if self.isDisconnected() {
            self.selected = location
        } else {
            locationError = LocationViewModel.cannotChangeWhenInProgress
        }
    }

    func clearError() {
        self.locationError = nil
    }
}
