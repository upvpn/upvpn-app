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

    /// green "7-DAY FREE TRIAL" badge, shown only to users eligible for the intro offer
    private func trialBadge(_ offer: Product.SubscriptionOffer) -> some View {
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
    }

    var body: some View {
        // laid out like the prepaid card: a title row with a short description right-aligned
        // in secondary, a divider, then the content row
        VStack(alignment: .leading, spacing: 5) {
            HStack {
                Text("Yearly plan").font(.headline)
                Spacer()
                Text("Just $3.33/mo")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Divider()

            HStack {
                // the trial badge while the user is eligible; the plan's selling point otherwise
                if let offer = freeTrialOffer, isEligibleForFreeTrial {
                    trialBadge(offer)
                } else {
                    Text("Unlimited data")
                        .font(.subheadline)
                }
                Spacer()
                PriceCapsule(text: "\(yearlyProduct.displayPrice)/year", isSelected: yearlyProduct == selectedProduct)
            }
            .padding(.vertical, 8)
        }
        .frame(maxWidth: .infinity)
        .contentShape(Rectangle())
        .onTapGesture {
            setSelectedProduct(yearlyProduct)
        }
    }
}
