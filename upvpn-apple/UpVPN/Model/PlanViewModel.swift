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

    private let prepaidProductIdentifiers = [
        "prepaid.499",
        "prepaid.999",
        "prepaid.1499",
        "prepaid.2499",
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
        self.planState = PlanState.loading
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
