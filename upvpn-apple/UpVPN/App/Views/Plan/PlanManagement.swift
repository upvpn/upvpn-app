//
//  PlanManagement.swift
//  UpVPN
//
//  Created by Himanshu on 8/5/24.
//

import SwiftUI
import StoreKit



struct PlanManagement: View {

    @EnvironmentObject var planViewModel: PlanViewModel

    @State private var isPurchaseErrorPresented = false

    var isRefreshable: Bool = false

    var body: some View {
        Group {
            switch planViewModel.planState {
            case .loading:
                ProgressView()
            case .plan(let userPlan):
                PlanView(
                    purchaseProduct: planViewModel.purchaseProduct,
                    userPlan: userPlan,
                    prepaidProducts: planViewModel.prepaidProducts,
                    yearlyProduct: planViewModel.yearlyProduct,
                    isPurchasing: planViewModel.isPurchasing,
                    completedSubscriptionPurchases: planViewModel.completedSubscriptionPurchases)
            case .errored:
                Button("Couldn't load plan, retry") {
                    planViewModel.reloadUserPlan()
                }
            }

        }
        .onAppear {
            planViewModel.reloadUserPlan()
        }
        .navigationTitle("Plan")
        #if os(macOS)
        // On macOS this view lives in sheets, which take their size from the content's ideal
        // size. Fix that size so neither swapping the loading, plan and error views nor the
        // plan changing after a purchase asks the sheet window to resize: the window cannot
        // resize while the StoreKit confirmation sheet is attached to it, and the content
        // then overflowed the sheet, anchored at its bottom left. Inside the account sheet's
        // navigation stack the stack's fixed size is proposed instead (see MainViewContent26),
        // so the minimum must stay below it or the content overflows that sheet too.
        .frame(minWidth: 400, idealWidth: 470, minHeight: 450, idealHeight: 560)
        #else
        .frame(minWidth: 400, minHeight: 450)
        #endif
        .onReceive(planViewModel.$purchaseError) { error in
            if let error = error, !error.isEmpty {
                isPurchaseErrorPresented = true
            }
        }
        .alert(
            "Purchase",
            isPresented: $isPurchaseErrorPresented,
            presenting: planViewModel.purchaseError
        ) { _ in
            Button(role: .cancel) {
                planViewModel.clearPurchaseError()
            } label: {
                Text("OK")
            }
        } message: { message in
            Text(message)
        }
        .modifier(IsRefreshable(isRefreshable: isRefreshable, onRefresh: {
            planViewModel.reloadProducts()
            planViewModel.reloadUserPlan()
        }))
    }
}

struct IsRefreshable: ViewModifier {
    var isRefreshable: Bool
    var onRefresh: () -> Void

    func body(content: Content) -> some View {
        if isRefreshable {
            content.refreshable {
                onRefresh()
            }
        } else {
            content
        }
    }
}

#Preview {
    PlanManagement()
}
