//
//  LoginView.swift
//  UpVPN
//
//  Created by Himanshu on 7/26/24.
//

import SwiftUI

struct PasswordField: View {
    @Binding var password: String
    @State private var showPassword: Bool = false

    var body: some View {
        HStack {
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
        .background(RoundedRectangle(cornerRadius: 9)
            .strokeBorder(Color.gray, lineWidth: 1)
        )
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
                    .background(RoundedRectangle(cornerRadius: 9)
                        .strokeBorder(Color.gray, lineWidth: 1)
                    )
                    #if os(iOS)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    #endif

                PasswordField(password: $authViewModel.userCredentials.password)


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
                    authViewModel.signIn()
                } label: {
                    if authViewModel.signInState == .signingIn {
                        if #available(macOS 13, iOS 15, *) {
                            ProgressView()
                                .modifier(ScaleEffectModifier())
                                .padding(.vertical, 5)
                                .frame(maxWidth: .infinity)
                        } else {
                            // on macOS 12 progress view spinner goes out of button boundary
                            Text("Sigining in")
                                .padding(.vertical, 5)
                                .frame(maxWidth: .infinity)
                        }
                    } else {
                        Text("Sign In")
                            .padding(.vertical, 5)
                            .frame(maxWidth: .infinity)
                    }
                }
                .keyboardShortcut(.defaultAction)
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
            authViewModel.signIn()
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
    LoginView()
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: { return true }))
}
