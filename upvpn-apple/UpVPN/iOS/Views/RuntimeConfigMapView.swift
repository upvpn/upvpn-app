//
//  RuntimeConfigMapView.swift
//  UpVPNiOS
//
//  Created by Himanshu on 9/3/24.
//

import SwiftUI

struct RuntimeConfigMapView<MapModifier: ViewModifier, 
                            ConfigModifier: ViewModifier,
                            PickerModifier: ViewModifier>: View {

    @Binding var showMapOrConfig: MapOrConfig

    var mapModifier:  MapModifier
    var configModifier: ConfigModifier
    var pickerModifier: PickerModifier

    var body: some View {
        if #available(iOS 17, *) {
            VStack {
                switch showMapOrConfig {
                case .map:
                    LocationsMapView()
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .modifier(mapModifier)
                case .config:
                    RuntimeConfigurationView()
                        .modifier(configModifier)
                }
            }
            .safeAreaInset(edge: .bottom) {
                Picker("", selection: $showMapOrConfig) {
                    Text("Runtime Configuration")
                        .tag(MapOrConfig.config)

                    Text("Map")
                        .tag(MapOrConfig.map)
                }
                .pickerStyle(.segmented)
                .modifier(pickerModifier)
            }
        } else {
            RuntimeConfigurationView()
        }
    }
}

#Preview {
    RuntimeConfigMapView(showMapOrConfig: .constant(.map),
                         mapModifier: LandscapeMapModifier(),
                         configModifier: LandscapeRuntimeConfigModifier(),
                         pickerModifier: LandscapePickerModifier())
}
