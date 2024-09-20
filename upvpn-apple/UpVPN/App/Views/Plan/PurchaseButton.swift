//
//  PurchaseButton.swift
//  UpVPN
//
//  Created by Himanshu on 8/13/24.
//

import SwiftUI
import StoreKit

private func productHeading(_ product: Product) -> String {
    return if product.id.starts(with: "prepaid") {
        "Prepaid balance never expires. A VPN session is charged $0.02/hr + $0.04/GB + $0.05 base."
    } else if product.id.starts(with: "subscription") {
        "Yearly at just $3.33/mo. Unlimited data."
    } else {
        "You will be charged \(product.displayPrice) for this purchase"
    }
}

private func buttonTitle(_ product: Product) -> String {
    if product.id.starts(with: "prepaid") {
        let centsSplit = product.id.splitToArray(separator: ".").reversed()
        let centsString = centsSplit.first
        var cents: Int32 = 0
        if let centsString = centsString {
            if let centsConverted = Int32(centsString) {
                cents = centsConverted
            }
        }

        var displayPrice = product.displayPrice

        if cents > 0 {
            displayPrice = formatCentsToDollars(cents)
        }

        return "Add \(displayPrice) to balance"
    } else if product.id.starts(with: "subscription") {
        return "Upgrade"
    } else {
        return "Buy Now"
    }
}

struct PurchaseButton: View {
    var selectedProduct: Product? = nil
    var isPurchasing: Bool = false
    var purchaseProduct: (Product) -> Void

    var body: some View {
        VStack(spacing: 0) {
            Text(selectedProduct == nil ? "" : productHeading(selectedProduct!))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .font(.subheadline)
                .padding()

            Button(action: {
                if let selectedProduct = selectedProduct {
                    purchaseProduct(selectedProduct)
                }
            }) {
                if isPurchasing {
                    ProgressView()
                        .padding(.vertical, 5)
                        .frame(maxWidth: .infinity)
                } else {
                    Text(selectedProduct == nil ? "Buy Now" : buttonTitle(selectedProduct!))
                        .fontWeight(.semibold)
                        .padding(.vertical, 5)
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .padding([.horizontal, .bottom])
            .padding([.horizontal, .bottom], 5)
            .disabled(selectedProduct == nil || isPurchasing)
        }
    }
}

#Preview {
    PurchaseButton(purchaseProduct: {p in })
}
