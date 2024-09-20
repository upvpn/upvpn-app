//
//  YearlyPlanView.swift
//  UpVPN
//
//  Created by Himanshu on 8/13/24.
//

import SwiftUI
import StoreKit

struct YearlyPlanView: View {

    var yearlyProduct: Product
    var selectedProduct: Product? = nil
    var setSelectedProduct: (Product) -> Void

    var body: some View {
        Group {
            HStack {
                VStack(alignment: .leading, spacing: 5) {
                    Text("Yearly plan").font(.headline)
                    Text("Get unlimited data").font(.caption).foregroundStyle(.gray)
                }
                Spacer()
                PriceCapsule(text: "\(yearlyProduct.displayPrice)/year", isSelected: yearlyProduct == selectedProduct)
                    .onTapGesture {
                       setSelectedProduct(yearlyProduct)
                    }
            }
            
        }
        .frame(maxHeight: 200)
    }
}
