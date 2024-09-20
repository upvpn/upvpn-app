//
//  Color+Extension.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import Foundation
import SwiftUI

extension Color {
    static var uSystemGroupedBackground: Color {
        #if os(iOS)
        return Color(uiColor: .systemGroupedBackground)
        #elseif os(macOS)
        return Color.clear
        #endif
    }

    static var uSecondarySystemGroupedBackground: Color {
        #if os(iOS)
        return Color(uiColor: .secondarySystemGroupedBackground)
        #elseif os(macOS)
        return Color(nsColor: .controlBackgroundColor)
        #endif
    }
}
