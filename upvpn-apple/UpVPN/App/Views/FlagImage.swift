//
//  FlagImage.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import SwiftUI
import FlagKit

struct FlagImage: View {
    var countryCode: String

    var body: some View {
        #if os(iOS)
        Image(uiImage: Flag(countryCode: countryCode)!.image(style: .roundedRect))
        #elseif os(macOS)
        Image(nsImage: Flag(countryCode: countryCode)!.originalImage)
            .clipShape(RoundedRectangle(cornerRadius: 3))
        #endif
    }
}

#Preview {
    FlagImage(countryCode: "US")
}
