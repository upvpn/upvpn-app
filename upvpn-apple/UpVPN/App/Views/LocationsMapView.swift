//
//  LocationsMapView.swift
//  UpVPNiOS
//
//  Created by Himanshu on 9/3/24.
//

import SwiftUI
import MapKit

func getLatLong(_ location: Location) -> (CLLocationDegrees, CLLocationDegrees)? {
    switch location.code {
    case "au_vic_melbourne": return (-37.813629, 144.963058)
    case "au_nsw_sydney": return (-33.872710,151.205694)
    case "br_sao_paulo": return (-23.579640,-46.655065)
    case "ca_qc_montreal": return (45.507773,-73.554461)
    case "ca_on_toronto": return (43.651605,-79.383125)
    case "cl_santiago": return (-33.451856,-70.650466)
    case "gb_london": return (51.503347,-0.079396)
    case "gb_manchester": return (53.479606,-2.245505)
    case "fi_helsinki": return (60.167928, 24.952984)
    case "fr_paris": return (48.856788, 2.351077)
    case "de_by_falkenstein": return (50.477179, 12.365762)
    case "de_he_frankfurt": return (50.110556, 8.680173)
    case "de_by_nuremberg": return (49.454473, 11.076937)
    case "in_ka_bengaluru": return (12.977405, 77.574234)
    case "in_tn_chennai": return (13.082881, 80.276002)
    case "in_delhi": return (28.626963, 77.215396)
    case "in_mh_mumbai": return (18.940100, 72.834659)
    case "id_jakarta": return (-6.212922, 106.848723)
    case "ireland": return (53.422433, -7.929837)
    case "il_tel_aviv": return (32.084454, 34.785621)
    case "it_milan": return (45.467175, 9.189664)
    case "jp_osaka": return (34.693726, 135.502162)
    case "jp_tokyo": return (35.689506, 139.691700)
    case "mx_mexico_city": return (19.430105, -99.133607)
    case "nl_amsterdam": return (52.401404, 4.931969)
    case "pl_warsaw": return (52.243427, 21.001797)
    case "singapore": return (1.304374, 103.824580)
    case "sa_johannesburg": return (-26.202270, 28.043630)
    case "kr_seoul": return (37.566983, 126.978235)
    case "es_madrid": return (40.419213, -3.692517)
    case "se_stockholm": return (59.327875, 18.053265)
    case "us_va_ashburn": return (39.046636, -77.471291)
    case "us_ga_atlanta": return (33.748188, -84.390865)
    case "us_il_chicago": return (41.883718, -87.632382)
    case "us_tx_dallas": return (32.775568, -96.795595)
    case "us_ca_fremont": return (37.552329, -121.983005)
    case "us_or_hillsboro": return (45.522667, -122.989020)
    case "us_hi_honolulu": return (21.304687, -157.857388)
    case "us_ca_los_angeles": return (34.053345, -118.242349)
    case "us_fl_miami": return (25.770843, -80.197636)
    case "us_nj_newark": return (40.732026, -74.174184)
    case "us_ny_newyork": return (40.712982, -74.007205)
    case "us_ohio": return (39.962522, -82.997972)
    case "us_oregon": return (44.941253, -123.029020)
    case "us_ca_san_francisco": return (37.779379, -122.418433)
    case "us_wa_seattle": return (47.603776, -122.330765)
    case "us_ca_silicon_valley": return (37.337213, -121.887090)
    case "us_virginia": return (37.540829, -77.433881)
    case "us_washington_dc": return (38.895438, -77.031281)
    default:
        return nil
    }
}

func getCoordinate(_ location: Location) -> CLLocationCoordinate2D? {
     getLatLong(location).map { (lat, long) in CLLocationCoordinate2DMake(lat, long) }
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
                    if let coordinate = getCoordinate(location) {
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
                    if let coordinate = getCoordinate(location) {
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
                if let coordinate = getCoordinate(location) {
                    mapCameraPosition = .region(MKCoordinateRegion(center: coordinate,
                                                                   span: coordinateSpan.toMKCoordinateSpan()))
                }
            }
        }
        .onChange(of: locationViewModel.selected) {
            if let location = locationViewModel.selected {
                if let coordinate = getCoordinate(location) {
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
