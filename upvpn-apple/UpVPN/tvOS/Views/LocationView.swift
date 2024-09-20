//
//  LocationView.swift
//  UpVPN
//
//  Created by Himanshu on 9/14/24.
//

import SwiftUI
import FlagKit

struct LocationView: View {
    var location: Location

    @EnvironmentObject var locationViewModel: LocationViewModel

    var body: some View {
        Button {
            withAnimation {
                locationViewModel.setSelected(location: location)
            }
        } label: {
            HStack(spacing: 15) {
                FlagImage(countryCode: location.countryCode)
                    .frame(maxHeight: .infinity)
                Text(location.displayText())
                    .font(.headline)
                    .fixedSize()
                Spacer()
                HStack(spacing: 20) {
                    if (location.code == locationViewModel.selected?.code) {
                        Image(systemName: "checkmark.circle").foregroundColor(Color.green)
                    }

                    Circle()
                        .fill(location.warmOrColdColor())
                        .frame(width: 24, height: 24)
                }
            }
            .padding()
            .cornerRadius(15)
        }
    }
}

#Preview {
    LocationView(location: Location.testLocation)
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
