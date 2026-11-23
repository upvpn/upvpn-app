//
//  PrepaidPlansView.swift
//  UpVPN
//
//  Created by Himanshu on 8/12/24.
//

import SwiftUI
import StoreKit

struct PrepaidPlansView: View {
    var prepaidProducts: [Product] = []
    var selectedProduct: Product? = nil
    var setSelectedProduct: (Product) -> Void

    #if os(tvOS)
    private let columns: [GridItem] = Array(repeating: .init(.flexible(), spacing: 40), count: 4)
    #else
    // flexible, not fixed: a localized promo price ("Rp 419.000 $5 FREE") can be wider
    // than 120pt, and a fixed column would let it overlap its neighbour
    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    #endif

    var body: some View {
        Group {
            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Text("Prepaid Credit").font(.headline)
                    Spacer()
                    Text("Top up balance")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Divider()

                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(prepaidProducts) { product in
                        PriceCapsule(text: product.compactDisplayPrice,
                                     isSelected: product == selectedProduct,
                                     bonus: product.prepaidBonus)
                            .onTapGesture {
                               setSelectedProduct(product)
                            }
                    }
                }
                .padding()
            }
            .frame(maxWidth: .infinity)

        }
        .frame(maxHeight: 200)
    }
}

#Preview {
    let planViewModel = PlanViewModel()
    return PrepaidPlansView(prepaidProducts: planViewModel.prepaidProducts, selectedProduct: nil, setSelectedProduct: { p in  })
}
