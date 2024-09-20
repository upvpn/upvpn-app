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

    var body: some View {
        List {

            Section("Current Plan") {
                switch userPlan {
                case .PayAsYouGo(let userPlanPayAsYouGo):
                    HStack {
                        Text("Pay as you go")
                        Spacer()
                        Text("Balance \(formatCentsToDollars(userPlanPayAsYouGo.balance))")
                    }
                case .AnnualSubscription:
                    VStack(alignment: .leading) {
                        Text("Yearly")
                        Divider()
                        Label("You're on the best plan", systemImage: "sparkles")
                    }
                }

            }

            if case .PayAsYouGo = userPlan {
                Section {
                    PrepaidPlansView(prepaidProducts: prepaidProducts,
                                     selectedProduct: selectedProduct,
                                     setSelectedProduct: { product in
                        self.selectedProduct = product
                    })
                }


                if let yearlyProduct = yearlyProduct {
                    Section {
                        YearlyPlanView(yearlyProduct: yearlyProduct,
                                       selectedProduct: selectedProduct,
                                       setSelectedProduct: { product in
                            self.selectedProduct = product
                        })
                    }
                }
            }
        }
        .safeAreaInset(edge: .bottom) {
            PurchaseButton(selectedProduct: selectedProduct, isPurchasing: isPurchasing, purchaseProduct: purchaseProduct)
        }
    }
}

#Preview {
    PlanView(userPlan: .PayAsYouGo(UserPlanPayAsYouGo(balance: 100)))
}

#Preview {
    PlanView(userPlan: .AnnualSubscription)
}
