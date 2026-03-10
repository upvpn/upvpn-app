//
//  LoginView.swift
//  UpVPN
//
//  Created by Himanshu on 7/26/24.
//

import SwiftUI
import AuthenticationServices
#if !os(tvOS)
import GoogleSignIn

struct ContinueWithGoogleButton: View {
    var action: () -> Void
    var height: CGFloat = 44

    // Google Blue #4285F4
    private static let googleBlue = Color(red: 0x42/255.0, green: 0x85/255.0, blue: 0xF4/255.0)

    private var logoSize: CGFloat {
        #if os(macOS)
        return 12
        #else
        return 14
        #endif
    }

    private var fontSize: CGFloat {
        #if os(macOS)
        return 12
        #else
        return 16
        #endif
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image("google_logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: logoSize, height: logoSize)
                    .padding(3)
                    .background(Color.white)
                    .cornerRadius(3)
                Text("Continue with Google")
                    .font(.system(size: fontSize, weight: .medium))
            }
            .frame(maxWidth: .infinity)
            .frame(height: height)
            .background(Self.googleBlue)
            .foregroundColor(.white)
            .cornerRadius(6)
        }
        .buttonStyle(.plain)
    }
}
#endif

struct SsoSectionView: View {
    @EnvironmentObject private var authViewModel: AuthViewModel
    @Environment(\.colorScheme) private var colorScheme

    @State private var appleButtonWidth: CGFloat? = nil

    private var appleButtonStyle: SignInWithAppleButton.Style {
        colorScheme == .dark ? .white : .black
    }

    private var ssoButtonHeight: CGFloat {
        #if os(macOS)
        return 32
        #else
        return 44
        #endif
    }

    var body: some View {
        VStack(spacing: 16) {
            if authViewModel.isSsoSigningIn {
                ProgressView()
                    .modifier(ScaleEffectModifier())
                    .padding(.vertical, 8)
            } else {
                SignInWithAppleButton(.continue) { request in
                    request.requestedScopes = [.email]
                } onCompletion: { result in
                    switch result {
                    case .success(let authorization):
                        if let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
                           let identityTokenData = credential.identityToken,
                           let idToken = String(data: identityTokenData, encoding: .utf8) {
                            let email = credential.email ?? JWTUtils.extractEmail(fromIdToken: idToken) ?? ""
                            authViewModel.signInWithApple(idToken: idToken, email: email)
                        }
                    case .failure(let error):
                        if (error as NSError).domain != ASAuthorizationError.errorDomain {
                            authViewModel.ssoErrorMessage = error.localizedDescription
                        }
                    }
                }
                .signInWithAppleButtonStyle(appleButtonStyle)
                .frame(height: ssoButtonHeight)
                .background(GeometryReader { geo in
                    Color.clear.onAppear {
                        appleButtonWidth = geo.size.width
                    }
                    .onChange(of: geo.size.width) { newWidth in
                        appleButtonWidth = newWidth
                    }
                })
                #if !os(tvOS)
                if GoogleSignInManager.shared.isAvailable {
                    ContinueWithGoogleButton(action: {
                        authViewModel.signInWithGoogle()
                    }, height: ssoButtonHeight)
                    .frame(maxWidth: appleButtonWidth)
                }
                #endif
            }
        }
        .disabled(authViewModel.signInState == .signingIn || authViewModel.isSigningUp || authViewModel.isSsoSigningIn)
    }
}

struct AuthView: View {
    @EnvironmentObject private var authViewModel: AuthViewModel

    @State private var isSignInErrorPresented = false
    @State private var isSignUpErrorPresented = false
    @State private var isSsoErrorPresented = false

    @AppStorage("userConsent") private var userConsent: Bool = true

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
            VStack(spacing: 16) {

                VStack(spacing: 5) {
                    WelcomeView(showSpinnner: false)
                    Text("UpVPN")
                        .font(.largeTitle.bold())

                    Text("A Modern Serverless VPN")
                        .font(.headline.weight(.thin))
                }

                SsoSectionView()

                HStack {
                    VStack { Divider() }
                    Text("or")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                    VStack { Divider() }
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
                .disabled(authViewModel.signInState == .signingIn || authViewModel.isSigningUp || authViewModel.isSsoSigningIn)

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
        .onReceive(authViewModel.$ssoErrorMessage) { error in
            if let error = error {
                if !error.isEmpty {
                    isSsoErrorPresented = true
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
        .alert("Sign In",
               isPresented: $isSsoErrorPresented,
               presenting: authViewModel.ssoErrorMessage
        ) { _ in
            Button(role: .cancel) {
                authViewModel.clearSsoError()
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
