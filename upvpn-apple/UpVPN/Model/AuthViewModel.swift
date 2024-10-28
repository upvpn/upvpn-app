//
//  AuthViewModel.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation

enum SignInState {
    case checkingLocal
    case notSignedIn
    case signedIn
    case signingIn
    case signingOut
}

enum AuthAction {
    case signUp
    case signIn
}

@MainActor
class AuthViewModel: ObservableObject {
    @Published var userCredentials: UserCredentials = UserCredentials(email: "", password: "")
    @Published var signInState: SignInState = SignInState.checkingLocal

    @Published var signedInUserEmail: String? = nil
    @Published var authDevice: Device? = nil

    @Published var signInErrorMessage: String? = nil
    @Published var signOutErrorMessage: String? = nil
    @Published var signUpErrorMessage: String? = nil

    @Published var signUpEmailCode: String = ""
    @Published var signUpEmailCodeRequestedAt: Date? = nil

    @Published var isSigningUp: Bool = false
    @Published var authAction: AuthAction = .signIn

    private var dataRepository: DataRepository

    private var isDisconnected: () -> Bool

    init(dataRepository: DataRepository, isDisconnected: @escaping () -> Bool) {
        self.dataRepository = dataRepository
        self.isDisconnected = isDisconnected
        self.checkLocal()
    }

    private func setSignedIn(email: String, device: Device) {
        self.signedInUserEmail = email
        self.authDevice = device
        self.signInState = SignInState.signedIn
    }

    private func checkLocal() {
        Task {
            let (authUser, device) = await self.dataRepository.isAuthenticated()
            await MainActor.run {
                if let authUser = authUser, let device = device {
                    self.setSignedIn(email: authUser, device: device)
                } else {
                    self.signInState = SignInState.notSignedIn
                }
            }
        }
    }

    func clearSignInError() {
        self.signInErrorMessage = nil
    }

    func clearSignUpError() {
        self.signUpErrorMessage = nil
    }

    func clearSignOutError() {
        self.signOutErrorMessage = nil
    }


    private func sanitizeUserCredentials() {
        // trim spaces from email
        self.userCredentials = UserCredentials(email: self.userCredentials.email.trimmingCharacters(in: .whitespacesAndNewlines),
                                          password: self.userCredentials.password)
    }

    func signIn() {
        self.clearSignInError()
        self.signInState = SignInState.signingIn

        self.sanitizeUserCredentials()

        Task {
            let result = await self.dataRepository.addDevice(userCredentials: self.userCredentials)

            await MainActor.run {
                switch result {
                case .success(let device):
                    self.setSignedIn(email: self.userCredentials.email, device: device)
                    self.userCredentials.password = ""
                case .failure(let dataRepoError):
                    self.signInState = SignInState.notSignedIn
                    self.signInErrorMessage = dataRepoError.message
                }
            }
        }
    }

    func signUp() {
        self.clearSignUpError()
        self.isSigningUp = true

        self.sanitizeUserCredentials()

        Task {
            let result = await self.dataRepository
                .signUp(userCredsWithCode:
                            self.userCredentials.toUserCredentialsWithCode(code: self.signUpEmailCode))
            await MainActor.run {
                switch result {
                case .success:
                    self.signUpEmailCode = ""
                    self.signIn()
                    self.authAction = .signIn
                case .failure(let dataRepoError):
                    self.signUpErrorMessage = dataRepoError.message
                }
                self.isSigningUp = false
            }
        }
    }

    func requestCode() {
        self.sanitizeUserCredentials()

        Task {
            let result = await self.dataRepository.requestCode(email: self.userCredentials.email)
            await MainActor.run {
                switch result {
                case .success:
                    self.signUpEmailCodeRequestedAt = Date()
                case .failure(let dataRepoError):
                    self.signUpErrorMessage = dataRepoError.message
                }
            }
        }
    }

    func onSubmit(_ userHasConsented: Bool) {
        if userHasConsented && !self.userCredentials.email.isEmpty && !self.userCredentials.password.isEmpty {
            switch authAction {
            case .signUp:
                if !self.signUpEmailCode.isEmpty {
                    self.signUp()
                }
            case .signIn:
                self.signIn()
            }
        }
    }

    // note: the signout caller must call stop on tunnel (if any)
    func signOut() {
        if !isDisconnected() {
            self.signOutErrorMessage = "cannot signout when VPN session is in progress"
        } else {
            self.clearSignOutError()
            let previous = self.signInState
            self.signInState = SignInState.signingOut

            Task {
                let result = await self.dataRepository.signOut()

                await MainActor.run {
                    if case .failure(let dataRepoError) = result {
                        self.signOutErrorMessage = dataRepoError.message
                        self.signInState = previous
                    } else {
                        self.signInState = .notSignedIn
                    }
                }
            }
        }
    }


}
