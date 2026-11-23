//
//  LocationsMapView.swift
//  UpVPNiOS
//
//  Created by Himanshu on 9/3/24.
//

import SwiftUI
import MapKit

extension Location {
    /// The pin of a location, from the API's `geo` field.
    var coordinate: CLLocationCoordinate2D? {
        geo.map { CLLocationCoordinate2DMake($0.latitude, $0.longitude) }
    }
}

@available(iOS 17, *)
enum CoordinateSpan {
    case small
    case large

    func toMKCoordinateSpan() -> MKCoordinateSpan {
        switch self {
        case .small:
            LocationsMapView.coordinateSpanSmall
        case .large:
            LocationsMapView.coordinateSpanLarge
        }
    }
}

@available(iOS 17, *)
struct LocationsMapView: View {

    @EnvironmentObject private var locationViewModel: LocationViewModel

    @State private var mapCameraPosition: MapCameraPosition = .automatic

    static var coordinateSpanSmall: MKCoordinateSpan = MKCoordinateSpan(latitudeDelta: 10, longitudeDelta: 10)
    static var coordinateSpanLarge: MKCoordinateSpan = MKCoordinateSpan(latitudeDelta: 50, longitudeDelta: 50)

    var coordinateSpan: CoordinateSpan = CoordinateSpan.small

    var body: some View {
        Map(position: $mapCameraPosition, selection: locationViewModel.selectionBinding) {
            ForEach(locationViewModel.locations) { location in
                if location == locationViewModel.selected {
                    if let coordinate = location.coordinate {
                        Annotation("", coordinate: coordinate) {
                            LocationView(location: location)
                                .id(locationViewModel.locationsLastUpdated)
                            #if os(tvOS)
                                .background(.ultraThinMaterial)
                            #else
                                .background(Color.uSecondarySystemGroupedBackground)
                            #endif
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                .environmentObject(locationViewModel)
                        }
                        .tag(location)
                    }
                } else {
                    if let coordinate = location.coordinate {
                        Marker(location.city, coordinate: coordinate)
                            .tag(location)
                    }
                }
            }
        }
        .ignoresSafeArea()
        .mapStyle(.standard)
        .onAppear {
            if let location = locationViewModel.selected {
                if let coordinate = location.coordinate {
                    mapCameraPosition = .region(MKCoordinateRegion(center: coordinate,
                                                                   span: coordinateSpan.toMKCoordinateSpan()))
                }
            }
        }
        .onChange(of: locationViewModel.selected) {
            if let location = locationViewModel.selected {
                if let coordinate = location.coordinate {
                    mapCameraPosition = .region(MKCoordinateRegion(center: coordinate, 
                                                                   span: coordinateSpan.toMKCoordinateSpan()))
                }
            }
        }
    }
}

@available(iOS 17, *)
#Preview {
    LocationsMapView()
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
