//
//  EmptyCardWithLogo.swift
//  UpVPN
//
//  Created by Himanshu on 7/30/24.
//

import SwiftUI

struct CardContainer<Content: View>: View {

    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        ZStack {
            Rectangle()
                .fill(Color.uSystemGroupedBackground)
                .ignoresSafeArea()
            content
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.uSecondarySystemGroupedBackground)
                .cornerRadius(10)
                .padding()
        }
    }
}

#Preview {
    CardContainer {
        WelcomeView(showSpinnner: false)
    }
}
