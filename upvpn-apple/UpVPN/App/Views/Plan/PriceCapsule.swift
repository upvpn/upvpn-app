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

    var body: some View {
        Text(text)
            .font(.system(size: 14, weight: .bold))
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
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

