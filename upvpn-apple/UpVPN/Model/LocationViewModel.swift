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

    private func updateRecents() {
        self.recentLocations = self.recentLocations.compactMap { recent in
            self.locations.first(where: { $0 == recent })
        }
    }

    private func reconcileSelected() {
        if let selected = self.selected {
            if let fresh = self.locations.first(where: { $0 == selected }) {
                // same location from the latest list, to update estimate and price
                self.selected = fresh
            } else if self.isDisconnected() {
                self.selected = self.locations.defaultLocation()
            }
        } else {
            self.selected = self.locations.defaultLocation()
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
                        self.reconcileSelected()
                        // upon fetching new locations
                        self.updateRecents()
                        self.locationsLastUpdated = Date()
                        self.loading = false
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
