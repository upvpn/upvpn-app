//
//  LocationView.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import SwiftUI
import FlagKit

struct LocationView: View {
    var location: Location

    @EnvironmentObject var locationViewModel: LocationViewModel

    var body: some View {
        HStack(spacing: 15) {
            FlagImage(countryCode: location.countryCode)
            Text(location.displayText())
                .font(.headline)
            Spacer()
            HStack(spacing: 20) {
                if (location.code == locationViewModel.selected?.code) {
                    Image(systemName: "checkmark.circle").foregroundColor(Color.green)
                }

                Circle()
                    .fill(location.warmOrColdColor())
                    .frame(width: 12, height: 12)
            }
        }
        .padding()
        .cornerRadius(15)
        // contentShape and onTapGuesture is only required for iOS 15
        // Because List selection doesn't work, follwing note from the link:
        // https://developer.apple.com/documentation/swiftui/list
        // In iOS 15, iPadOS 15, and tvOS 15 and earlier, lists support selection 
        // only in edit mode, even for single selections.
        .contentShape(Rectangle())
        // contentShape for: https://stackoverflow.com/questions/57191013/swiftui-cant-tap-in-spacer-of-hstack
        .onTapGesture {
            locationViewModel.setSelected(location: location)
        }
    }
}

#Preview {
    LocationView(location: Location.testLocation)
        .environmentObject(LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
