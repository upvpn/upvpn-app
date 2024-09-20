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

    @FocusState private var isFocused: Bool

    var body: some View {
        HStack(alignment: .center) {
            Text(text)
                .font(.headline)
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
