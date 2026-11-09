//
//  PlanView.swift
//  UpVPN
//
//  Created by Himanshu on 8/13/24.
//

import SwiftUI
import StoreKit

struct PlanView: View {
    var purchaseProduct: (Product) -> Void = {_product in }
    var userPlan: UserPlan
    var prepaidProducts: [Product] = []
    var yearlyProduct: Product? = nil
    var isPurchasing: Bool = false

    @State var selectedProduct: Product? = nil
    @State private var isEligibleForFreeTrial: Bool = false

    var body: some View {
        planSections
            .safeAreaInset(edge: .bottom) {
                PurchaseButton(selectedProduct: selectedProduct, isEligibleForFreeTrial: isEligibleForFreeTrial, isPurchasing: isPurchasing, purchaseProduct: purchaseProduct)
            }
            .task(id: yearlyProduct?.id) {
                if let subscription = yearlyProduct?.subscription {
                    isEligibleForFreeTrial = await subscription.isEligibleForIntroOffer
                } else {
                    isEligibleForFreeTrial = false
                }
            }
    }

    /// The sections in the platform's grouped container: a List on iOS and tvOS; on macOS a
    /// grouped Form, which draws each section in a rounded box as System Settings does (a
    /// List there is a flat, sidebar-like list with tall section headers).
    @ViewBuilder
    private var planSections: some View {
        #if os(macOS)
        Form {
            sections
        }
        // grouped where available (macOS 13+), see SettingsView
        .modifier(FormModifier())
        #else
        List {
            sections
        }
        #endif
    }

    @ViewBuilder
    private var sections: some View {
            // laid out like the prepaid and yearly cards: a title row, a divider, then the
            // content, rather than a section header floating above the card
            Section {
                VStack(alignment: .leading, spacing: 5) {
                    Text("Current Plan").font(.headline)

                    Divider()

                    switch userPlan {
                    case .PayAsYouGo(let userPlanPayAsYouGo):
                        HStack {
                            Text("Pay as you go")
                            Spacer()
                            Text("Balance \(formatCentsToDollars(userPlanPayAsYouGo.balance))")
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 8)
                    case .AnnualSubscription(let userPlanAnnualSubscription):
                        HStack {
                            Text(userPlan.displayName)
                            Spacer()
                            if let startedDate = userPlanAnnualSubscription.startedDate {
                                Text("Since \(startedDate.formatted(date: .abbreviated, time: .omitted))")
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .padding(.vertical, 8)

                        Divider()

                        // every plan carries a pay as you go balance: it pays for premium locations
                        VStack(alignment: .leading, spacing: 5) {
                            HStack {
                                Text("Pay as you go")
                                Spacer()
                                Text("Balance \(formatCentsToDollars(userPlan.balance))")
                                    .foregroundStyle(.secondary)
                            }
                            Text("Used for premium locations")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 8)
                    }
                }
                .frame(maxWidth: .infinity)
            }

            // prepaid credit is for sale on every plan: premium locations are billed from
            // the pay as you go balance even on yearly
            Section {
                PrepaidPlansView(prepaidProducts: prepaidProducts,
                                 selectedProduct: selectedProduct,
                                 setSelectedProduct: { product in
                    self.selectedProduct = product
                })
            }

            // the yearly plan is only for sale to pay as you go users
            if case .PayAsYouGo = userPlan, let yearlyProduct = yearlyProduct {
                Section {
                    YearlyPlanView(yearlyProduct: yearlyProduct,
                                   selectedProduct: selectedProduct,
                                   isEligibleForFreeTrial: isEligibleForFreeTrial,
                                   setSelectedProduct: { product in
                        self.selectedProduct = product
                    })
                }
            }
    }
}

#Preview {
    PlanView(userPlan: .PayAsYouGo(UserPlanPayAsYouGo(balance: 100)))
}

#Preview {
    PlanView(userPlan: .AnnualSubscription(UserPlanAnnualSubscription(balance: 250, startedAt: "2026-01-15T10:00:00Z")))
}
