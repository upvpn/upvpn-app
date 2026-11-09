//
//  PriceCapsule.swift
//  UpVPN
//
//  Created by Himanshu on 8/12/24.
//

import SwiftUI

struct PriceCapsule: View {
    let text: String
    let isSelected: Bool
    /// short promo drawn beside the price, e.g. "$5 FREE"
    var bonus: String? = nil

    private var priceText: some View {
        Text(text)
            .font(.system(size: 14, weight: .bold))
    }

    private var bonusText: some View {
        Text(bonus ?? "")
            .font(.system(size: 10, weight: .semibold))
    }

    /// Price and bonus side by side when they fit the column; otherwise the bonus moves
    /// under the price as a whole, so a long localized price ("Rp 419.000 $5 FREE") never
    /// breaks mid-text or spills into the neighbouring chip.
    @ViewBuilder
    private var label: some View {
        if bonus == nil {
            priceText
        } else if #available(iOS 16, macOS 13, tvOS 16, *) {
            ViewThatFits(in: .horizontal) {
                HStack(spacing: 4) {
                    priceText
                    bonusText
                }
                VStack(spacing: 2) {
                    priceText
                    bonusText
                }
            }
        } else {
            HStack(spacing: 4) {
                priceText
                bonusText
            }
        }
    }

    var body: some View {
        label
            .multilineTextAlignment(.center)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            // same size for every chip, with room for "$25 $5 FREE" inside a 120pt grid column
            .frame(minWidth: 110)
            .background(
                Capsule()
                    .fill(Color.blue.opacity(0.1))
                    .overlay(
                        Capsule()
                            .stroke(isSelected ? Color.blue : Color.clear, lineWidth: 2)
                    )
            )
            .foregroundColor(.blue)
    }
}

