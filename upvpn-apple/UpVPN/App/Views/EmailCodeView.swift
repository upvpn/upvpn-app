//
//  EmailCodeView.swift
//  UpVPN
//
//  Created by Himanshu on 10/8/24.
//

import SwiftUI

struct EmailCodeView: View {

    @EnvironmentObject private var authViewModel: AuthViewModel

    @State private var buttonText: String = "Email Code"
    @State private var buttonEnabled: Bool = true

    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        HStack {
            TextField("Enter 6-digit code",
                      text: $authViewModel.signUpEmailCode)
            .autocorrectionDisabled(true)
            #if os(iOS)
            .keyboardType(.numberPad)
            #endif

            Button(action: {
                authViewModel.requestCode()
            }) {
                Text(buttonText)
            }
            .disabled(!buttonEnabled || authViewModel.isSigningUp || authViewModel.userCredentials.email.isEmpty)
        }
        .padding(12)
        .textFieldStyle(PlainTextFieldStyle())
        .background(RoundedRectangle(cornerRadius: 9)
            .strokeBorder(Color.gray, lineWidth: 1)
        )
        .onReceive(timer) { _ in
            updateButtonText()
        }
    }

    private func updateButtonText() {
        let interval = Date().timeIntervalSince(authViewModel.signUpEmailCodeRequestedAt ?? Date.distantPast)
        if interval <= 60 {
            if interval < 3 {
                buttonText = "Email: code sent"
            } else {
                buttonText = "Resend in \(Int(60 - interval))"
            }
            buttonEnabled = false
        } else {
            buttonText = "Email Code"
            buttonEnabled = true
        }

    }
}


#Preview {
    EmailCodeView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
