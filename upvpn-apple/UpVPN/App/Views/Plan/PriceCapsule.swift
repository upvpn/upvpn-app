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

    var body: some View {
        HStack(spacing: 4) {
            Text(text)
                .font(.system(size: 14, weight: .bold))
            if let bonus = bonus {
                Text(bonus)
                    .font(.system(size: 10, weight: .semibold))
            }
        }
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

