//
//  PriceCapsule.swift
//  UpVPN
//
//  Created by Himanshu on 9/15/24.
//

import SwiftUI

struct PriceCapsule: View {
    let text: String
    let isSelected: Bool
    /// short promo drawn beside the price, e.g. "$5 FREE"
    var bonus: String? = nil

    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(alignment: .center) {
            // a tvOS grid column is wide enough for price and bonus on one line
            HStack(spacing: 8) {
                Text(text)
                    .font(.headline)
                if let bonus = bonus {
                    Text(bonus)
                        .font(.caption.bold())
                }
            }
                .scaledToFit()
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 15)
                        .fill(isFocused ? Color.white : Color.blue.opacity(0.1))
                        .overlay(
                            RoundedRectangle(cornerRadius: 15)
                                .stroke(isSelected ? Color.blue : Color.clear, lineWidth: 2)
                        )
                )
                .foregroundColor(.blue)
        }
        .focusable()
        .focused($isFocused)
    }
}
