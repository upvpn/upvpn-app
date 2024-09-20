//
//  SignOutView.swift
//  UpVPN
//
//  Created by Himanshu on 8/16/24.
//

import SwiftUI

struct SignOutView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    @State private var isConfirming = false

    var body: some View {
        if let _ = authViewModel.signedInUserEmail {
            HStack {
                Button {
                    isConfirming = true
                } label: {
                    SignOutLabel(signInState: authViewModel.signInState)
                }
                .disabled(authViewModel.signInState == .signingOut)
                .confirmationDialog("Are you sure?", isPresented: $isConfirming) {
                    Button {
                        authViewModel.signOut()
                    } label: {
                        SignOutLabel(signInState: authViewModel.signInState)
                    }
                    .disabled(authViewModel.signInState == .signingOut)

                    Button("Cancel", role: .cancel) {
                        isConfirming = false
                    }
                }
            }
        } else {
            Button("Unauthenticated") {}
        }
    }
}

struct SignOutLabel : View {
    var signInState: SignInState

    var body: some View {
        if signInState == .signingOut {
            Text("Signing Out")
        } else {
            Text("Sign Out")
        }
    }
}

#Preview {
    SignOutView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return false }))
}
