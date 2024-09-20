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
                    isPurchasing: planViewModel.isPurchasing)
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
        .frame(minWidth: 400, minHeight: 400)
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
