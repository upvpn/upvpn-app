//
//  YearlyPlanView.swift
//  UpVPN
//
//  Created by Himanshu on 8/13/24.
//

import SwiftUI
import StoreKit

private func trialPeriodText(_ offer: Product.SubscriptionOffer) -> String {
    let value = offer.period.value
    switch offer.period.unit {
    case .day:
        return "\(value)-DAY"
    case .week:
        return "\(value)-WEEK"
    case .month:
        return "\(value)-MONTH"
    case .year:
        return "\(value)-YEAR"
    @unknown default:
        return "\(value)-DAY"
    }
}

struct YearlyPlanView: View {

    var yearlyProduct: Product
    var selectedProduct: Product? = nil
    var isEligibleForFreeTrial: Bool = false
    var setSelectedProduct: (Product) -> Void

    private var freeTrialOffer: Product.SubscriptionOffer? {
        guard let offer = yearlyProduct.subscription?.introductoryOffer,
              offer.paymentMode == .freeTrial else {
            return nil
        }
        return offer
    }

    var body: some View {
        Group {
            if let offer = freeTrialOffer, isEligibleForFreeTrial {
                // Free trial view
                HStack {
                    VStack(alignment: .leading) {
                        HStack(spacing: 6) {
                            Image(systemName: "gift.fill")
                                .foregroundColor(.white)
                                .font(.caption)
                            Text("\(trialPeriodText(offer)) FREE TRIAL")
                                .font(.caption.bold())
                                .foregroundColor(.white)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Capsule().fill(Color.green))

                        Text("Yearly plan").font(.headline)
                        Text("Unlimited data.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    PriceCapsule(text: "\(yearlyProduct.displayPrice)/year", isSelected: yearlyProduct == selectedProduct)
                }
                .contentShape(Rectangle())
                .onTapGesture {
                    setSelectedProduct(yearlyProduct)
                }
            } else {
                // Regular yearly plan view
                HStack {
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Yearly plan").font(.headline)
                        Text("Get unlimited data").font(.caption).foregroundStyle(.gray)
                    }
                    Spacer()
                    PriceCapsule(text: "\(yearlyProduct.displayPrice)/year", isSelected: yearlyProduct == selectedProduct)
                }
                .onTapGesture {
                   setSelectedProduct(yearlyProduct)
                }
            }
        }
        .frame(maxHeight: 200)
    }
}
