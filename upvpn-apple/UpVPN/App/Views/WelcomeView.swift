//
//  WelcomeScreenView.swift
//  UpVPN
//
//  Created by Himanshu on 7/26/24.
//

import SwiftUI

struct WelcomeView: View {
    var showSpinnner = true
    var body: some View {
        VStack(spacing: 15) {
            Image("upvpn")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 100, height: 100)
            if showSpinnner {
                ProgressView()
            }
        }
        .frame(minWidth: 325, maxWidth: .infinity)
    }
}

#Preview {
    WelcomeView()
}
