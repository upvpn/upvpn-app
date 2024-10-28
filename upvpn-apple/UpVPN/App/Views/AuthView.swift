//
//  LoginView.swift
//  UpVPN
//
//  Created by Himanshu on 7/26/24.
//

import SwiftUI

struct AuthView: View {
    @EnvironmentObject private var authViewModel: AuthViewModel

    @State private var isSignInErrorPresented = false
    @State private var isSignUpErrorPresented = false

    @AppStorage("userConsent") private var userConsent: Bool = false

    @State private var dataForUserConsentIsPresented = false

    private var buttonDisabled: Bool {
        return !userConsent
              || authViewModel.signInState == .signingIn
              || authViewModel.isSigningUp
              || (authViewModel.authAction == .signUp
                  && UInt32(authViewModel.signUpEmailCode) ?? 0 <= UInt32(99999))
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 35) {

                VStack(spacing: 5) {
                    WelcomeView(showSpinnner: false)
                    Text("UpVPN")
                        .font(.largeTitle.bold())

                    Text("A Modern Serverless VPN")
                        .font(.headline.weight(.thin))
                }

                Picker("", selection: $authViewModel.authAction) {
                    Text("Sign Up")
                        .tag(AuthAction.signUp)

                    Text("Sign In")
                        .tag(AuthAction.signIn)
                }
                .pickerStyle(.segmented)
                .labelsHidden()

                VStack(spacing: 20) {
                    TextField("Email", text: $authViewModel.userCredentials.email)
                        .padding(12)
                        .textFieldStyle(PlainTextFieldStyle())
                        .background(RoundedRectangle(cornerRadius: 9)
                            .strokeBorder(Color.gray, lineWidth: 1)
                        )
#if os(iOS)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)
#endif

                    PasswordField(password: $authViewModel.userCredentials.password)

                    if case .signUp = authViewModel.authAction {
                        EmailCodeView()
                    }

                    Toggle(isOn: $userConsent) {
                        Text("Agree to associate device data to your account")
                            .font(.caption)
                            .foregroundStyle(.blue)
                            .onTapGesture {
                                dataForUserConsentIsPresented.toggle()
                            }
                    }
                    .padding(.horizontal)


                    Button {
                        authViewModel.onSubmit(userConsent)
                    } label: {
                        if authViewModel.signInState == .signingIn || authViewModel.isSigningUp {
                            if #available(macOS 13, iOS 15, *) {
                                ProgressView()
                                    .modifier(ScaleEffectModifier())
                                    .padding(.vertical, 5)
                                    .frame(maxWidth: .infinity)
                            } else {
                                // on macOS 12 progress view spinner goes out of button boundary
                                Text(authViewModel.isSigningUp ? "Signing Up": "Signing In")
                                    .padding(.vertical, 5)
                                    .frame(maxWidth: .infinity)
                            }
                        } else {
                            Text(authViewModel.authAction == .signIn ? "Sign In": "Create Account")
                                .padding(.vertical, 5)
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .keyboardShortcut(.defaultAction)
                    .buttonStyle(.borderedProminent)
                    .disabled(buttonDisabled)

                }
                .disabled(authViewModel.signInState == .signingIn || authViewModel.isSigningUp)


                HStack {
                    Text("""
By using UpVPN.app you agree to our [Terms](https://upvpn.app/terms-of-service) and [Privacy Policy](https://upvpn.app/privacy-policy)
""")
                }
                .font(.caption)
                .multilineTextAlignment(.leading)

            }
            .padding()
        }
        .frame(minWidth: 400, maxWidth: .infinity)
        .onReceive(authViewModel.$userCredentials) { _ in
            authViewModel.clearSignInError()
            authViewModel.clearSignUpError()
        }
        .onReceive(authViewModel.$signInErrorMessage)  { error in
            if let error = error {
                if !error.isEmpty {
                    isSignInErrorPresented = true
                }
            }
        }
        .onReceive(authViewModel.$signUpErrorMessage) { error in
            if let error = error {
                if !error.isEmpty {
                    isSignUpErrorPresented = true
                }
            }
        }
        .onSubmit {
            authViewModel.onSubmit(userConsent)
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
        .alert("Sign Up",
               isPresented: $isSignUpErrorPresented,
               presenting: authViewModel.signUpErrorMessage
        ) { _ in
            Button(role: .cancel) {
                authViewModel.clearSignUpError()
            } label: {
                Text("OK")
            }
        } message: { error in
            Text(error)
        }
        .sheet(isPresented: $dataForUserConsentIsPresented) {
            if #available(iOS 16, macOS 13, *) {
               UserDataConsent()
                    .presentationDragIndicator(.visible)
                    .presentationDetents([.medium, .large])

            } else {
                UserDataConsent()
                    #if os(macOS)
                    .toolbar {
                        // on macOS 12 ESC doesnt close it, hence provide a button
                        if #unavailable(macOS 13) {
                            Button {
                                dataForUserConsentIsPresented.toggle()
                            } label : {
                                Text("Close")
                            }
                        }
                    }
                    #endif
            }
        }
    }
}

#Preview {
    AuthView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
