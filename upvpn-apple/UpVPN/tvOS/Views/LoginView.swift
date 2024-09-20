//
//  LoginView.swift
//  UpVPN
//
//  Created by Himanshu on 9/15/24.
//

import SwiftUI

struct PasswordField: View {
    @Binding var password: String
    @State private var showPassword: Bool = false

    var body: some View {
        HStack(spacing: 20) {
            if showPassword {
                TextField("Password", text: $password)
            } else {
                SecureField("Password", text: $password)
            }

            Button(action: {
                showPassword.toggle()
            }) {
                Image(systemName: showPassword ? "eye" : "eye.slash")
                    .foregroundColor(.gray)
            }
        }
        .padding(12)
        .textFieldStyle(PlainTextFieldStyle())
        .keyboardType(.default)
    }
}

struct LoginView: View {
    @EnvironmentObject private var authViewModel: AuthViewModel

    @State private var isSignInErrorPresented = false

    @AppStorage("userConsent") private var userConsent: Bool = false

    @State private var dataForUserConsentIsPresented = false

    var body: some View {
        VStack(spacing: 35) {

            VStack(spacing: 5) {
                WelcomeView(showSpinnner: false)
                Text("UpVPN")
                    .font(.largeTitle.bold())

                Text("A Modern Serverless VPN")
                    .font(.headline.weight(.thin))
            }


            VStack(spacing: 20) {
                TextField("Email", text: $authViewModel.userCredentials.email)
                    .padding(12)
                    .textFieldStyle(PlainTextFieldStyle())
                    .keyboardType(.emailAddress)


                PasswordField(password: $authViewModel.userCredentials.password)


                HStack(spacing: 20) {
                    Button {  dataForUserConsentIsPresented.toggle() } label: {
                        Text("Associate device data to your account")
                    }

                    Toggle("Agree", isOn: $userConsent)
                }

                Button {
                    if userConsent {
                        authViewModel.signIn()
                    }
                } label: {
                    if authViewModel.signInState == .signingIn {
                            ProgressView()
                                .padding(.vertical, 5)
                                .frame(maxWidth: .infinity)
                    } else {
                        Text("Sign In")
                            .padding(.vertical, 5)
                            .frame(maxWidth: .infinity)
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(!userConsent)

            }
            .disabled(authViewModel.signInState == .signingIn)


            HStack {
                Text("""
By using UpVPN.app you agree to our [Terms](https://upvpn.app/terms-of-service) and [Privacy Policy](https://upvpn.app/privacy-policy)
""")
            }
            .font(.caption)
            .multilineTextAlignment(.leading)

        }
        .padding()
        .frame(minWidth: 400, maxWidth: .infinity)
        .onReceive(authViewModel.$userCredentials) { _ in
            authViewModel.clearSignInError()
        }
        .onReceive(authViewModel.$signInErrorMessage)  { error in
            if let error = error {
                if !error.isEmpty {
                    isSignInErrorPresented = true
                }
            }
        }
        .onSubmit {
            if userConsent {
                authViewModel.signIn()
            }
        }
        .alert(
            "Sign In",
            isPresented: $isSignInErrorPresented,
            presenting: authViewModel.signInErrorMessage
        ) { _ in
            Button(role: .cancel) {
                authViewModel.clearSignInError()
            } label: {
                Text("OK")
            }
        } message: { error in
            Text(error)
        }
        .sheet(isPresented: $dataForUserConsentIsPresented) {
           UserDataConsent()
        }
    }
}

#Preview {
    LoginView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
