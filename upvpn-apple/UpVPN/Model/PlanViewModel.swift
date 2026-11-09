//
//  PlanViewModel.swift
//  UpVPN
//
//  Created by Himanshu on 8/12/24.
//

import Foundation
import StoreKit

extension Transaction {
    var environmentString: String {
        if #available(iOS 16, macOS 13, *) {
            self.environment.rawValue
        } else {
            self.environmentStringRepresentation
        }
    }
}

/// "$25.00" -> "$25"; prices with real cents ("$4.99") are left alone
private func stripZeroCents(_ price: String) -> String {
    for zeroCents in [".00", ",00"] where price.hasSuffix(zeroCents) {
        return String(price.dropLast(zeroCents.count))
    }
    return price
}

extension Product {
    /// Cents credited to the pay as you go balance, carried by the product id
    /// ("prepaid.3000" credits 3000). The store charges the product's own price, which may be
    /// lower than the credit (prepaid.3000 sells for $25 and credits $30).
    var prepaidCreditCents: Int32? {
        guard id.starts(with: "prepaid"),
              let centsString = id.splitToArray(separator: ".").last,
              let cents = Int32(centsString), cents > 0 else {
            return nil
        }
        return cents
    }

    /// store price without a ".00" fraction, so a chip reads "$25" rather than "$25.00"
    var compactDisplayPrice: String {
        stripZeroCents(displayPrice)
    }

    /// the credited amount without a ".00" fraction, e.g. "$30"
    var compactPrepaidCredit: String? {
        prepaidCreditCents.map { stripZeroCents(formatCentsToDollars($0)) }
    }

    /// promo drawn on the chip of a prepaid product whose credit exceeds its price
    var prepaidBonus: String? {
        switch id {
        case "prepaid.3000": return "$5 FREE"
        default: return nil
        }
    }

    /// heading over the purchase button for a promo prepaid product,
    /// e.g. "Pay $25 receive $30 in usage credit. That's $5 free!"
    var prepaidPromoHeading: String? {
        switch id {
        case "prepaid.3000":
            let credit = compactPrepaidCredit ?? "$30"
            return "Pay \(compactDisplayPrice) receive \(credit) in usage credit. That's $5 free!"
        default:
            return nil
        }
    }
}

@MainActor
class PlanViewModel: ObservableObject {
    @Published var planState: PlanState = PlanState.loading
    @Published var prepaidProducts: [Product] = []
    @Published var yearlyProduct: Product? = nil

    @Published var purchaseError: String? = nil
    @Published var isPurchasing: Bool = false

    enum PlanState {
        case loading
        case plan(UserPlan)
        case errored(String)
    }

    // the id carries the credited cents: prepaid.3000 is priced $24.99 and credits $30
    // ("$25 + $5 free", as on web). prepaid.2499 is no longer for sale.
    private let prepaidProductIdentifiers = [
        "prepaid.499",
        "prepaid.999",
        "prepaid.1499",
        "prepaid.3000",
    ]

    private let yearlyProductIdentifier = "subscription.yearly"

    private var planRepository: PlanRepository = PlanRepository.shared

    var updateListenerTask: Task<Void, Error>? = nil

    init() {
        updateListenerTask = listenForTransactions()
        reloadProducts()
    }

    deinit {
        updateListenerTask?.cancel()
    }

    func listenForTransactions() -> Task<Void, Error> {
        return Task.detached { [weak self] in
            for await result in Transaction.unfinished {
                do {
                    let transaction = try await self?.checkVerified(result)
                    if let transaction = transaction {
                        try await self?.planRepository.processTransaction(environment: transaction.environmentString, 
                                                                          id: transaction.id).get()
                        await transaction.finish()
                    }
                } catch {
                    print("transaction listener error: \(error)")
                }
            }
        }
    }

    func reloadProducts() {
        Task {
            do {
                var productIdentifiers: [String] = []
                productIdentifiers.append(contentsOf: prepaidProductIdentifiers)
                productIdentifiers.append(yearlyProductIdentifier)
                let storeProducts = try await Product.products(for: productIdentifiers)

                var fetchedPrepaidProducts: [Product] = []
                for product in storeProducts {
                    switch product.type {
                    case .consumable:
                        fetchedPrepaidProducts.append(product)
                    case .autoRenewable:
                        await MainActor.run {
                            yearlyProduct = product
                        }
                    default:
                        print("unknown product")
                    }
                }

                fetchedPrepaidProducts.sort(by:  { p1, p2 in
                    p1.price < p2.price
                })

                await MainActor.run {
                    self.prepaidProducts = fetchedPrepaidProducts
                }
            } catch {
                print("failed to fetch products from app store: \(error)")
            }
        }
    }

    func reloadUserPlan() {
        // Keep the loaded plan on screen while it refreshes (after a purchase, on pull to
        // refresh) instead of flashing a spinner: besides the flicker, swapping the whole
        // plan view for a small ProgressView changes the content size, and on macOS the
        // sheet window cannot follow while the StoreKit confirmation sheet is still attached
        // to it, which left the view displaced inside the sheet after a purchase.
        if case .plan = planState {} else {
            self.planState = PlanState.loading
        }
        Task {
            let userPlan = await planRepository.getUserPlan()

            await MainActor.run {
                switch userPlan {
                case .success(let userPlan):
                    self.planState = .plan(userPlan)
                case .failure(let planError):
                    self.planState = .errored(planError.message)
                }
            }
        }
    }

    func purchaseProduct(_ product: Product) {
        self.isPurchasing = true
        Task {
            do {
                let device = try await DeviceStore.getDevice().get()
                
                guard let device = device else {
                    throw ErrorMessage(message: "no device found")
                }

                let result = try await product.purchase(options: 
                                                            Set(arrayLiteral: Product.PurchaseOption.appAccountToken(device.uniqueId)))

                switch result {
                case .success(let verificationResult):
                    let transaction = try checkVerified(verificationResult)
                    try await self.planRepository.processTransaction(environment: transaction.environmentString,
                                                                     id: transaction.id).get()

                    await transaction.finish()
                    // purchase was successful reload updated plan
                    await MainActor.run {
                        self.reloadUserPlan()
                    }
                case .userCancelled, .pending:
                    print("")
                default:
                    print("")
                }
            } catch {
                await MainActor.run {
                    purchaseError = "\(error)"
                }
            }

            await MainActor.run {
                self.isPurchasing = false 
            }
        }
    }

    func clearPurchaseError() {
        self.purchaseError = nil
    }

    private func checkVerified<T>(_ result: VerificationResult<T>)  throws ->  T {
        switch result {
        case .unverified:
            throw ErrorMessage(message: "failed verification")
        case .verified(let safe):
            return safe
        }
    }



}
