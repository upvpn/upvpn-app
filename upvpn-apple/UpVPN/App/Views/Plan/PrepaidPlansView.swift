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
    private let columns = [
        GridItem(.fixed(120)),
        GridItem(.fixed(120))
    ]
    #endif

    var body: some View {
        Group {
            VStack(alignment: .leading, spacing: 5) {
                Text("Prepaid Credit").font(.headline)
                Text("Add to Pay-as-you-go balance").font(.caption).foregroundStyle(.gray)

                Divider()

                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(prepaidProducts) { product in
                        PriceCapsule(text: product.displayPrice, isSelected: product == selectedProduct)
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
