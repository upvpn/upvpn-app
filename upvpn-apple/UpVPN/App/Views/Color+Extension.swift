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
        #elseif os(tvOS)
        return Color(uiColor: .clear)
        #elseif os(macOS)
        // the window as is, as the grouped form of System Settings leaves it: it takes the
        // wallpaper tint, and cards (CardBackground) are a translucent fill over it
        return Color.clear
        #endif
    }

    static var uSecondarySystemGroupedBackground: Color {
        #if os(iOS)
        return Color(uiColor: .secondarySystemGroupedBackground)
        #elseif os(tvOS)
        return Color(uiColor: .clear)
        #elseif os(macOS)
        return Color(nsColor: .controlBackgroundColor)
        #endif
    }
}

/// Background of a card (home card, recent locations, stats, welcome), with rounded corners.
///
/// On iOS it is the secondary grouped background, as cells of an inset grouped list have. On
/// macOS it is the tertiary system fill, as measured on the section boxes of System Settings
/// (a grouped form) in both appearances: 5% black over the white window in light, 5% white
/// over the dark window in dark.
struct CardBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(CardBackground.color)
            .cornerRadius(10)
    }

    static var color: Color {
        #if os(macOS)
        if #available(macOS 14, *) {
            return Color(nsColor: .tertiarySystemFill)
        }
        return Color(nsColor: NSColor(name: nil) { appearance in
            appearance.bestMatch(from: [.aqua, .darkAqua]) == .darkAqua
                ? NSColor.white.withAlphaComponent(0.05)
                : NSColor.black.withAlphaComponent(0.05)
        })
        #else
        return Color.uSecondarySystemGroupedBackground
        #endif
    }
}

#if os(macOS)
/// A List as a card on macOS: a List there draws no box of its own, so its background is
/// hidden (macOS 13+) and the card background drawn behind it.
struct ListCard: ViewModifier {
    func body(content: Content) -> some View {
        if #available(macOS 13, *) {
            content
                .scrollContentBackground(.hidden)
                .modifier(CardBackground())
        } else {
            content
                .modifier(CardBackground())
        }
    }
}
#endif
