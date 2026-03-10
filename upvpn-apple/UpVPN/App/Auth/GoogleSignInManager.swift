//
//  GoogleSignInManager.swift
//  UpVPN
//
//  Created by Himanshu on 3/10/26.
//

#if !os(tvOS)
import Foundation
import GoogleSignIn

class GoogleSignInManager {
    static let shared = GoogleSignInManager()

    var isAvailable: Bool {
        let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String
        return clientID != nil && !clientID!.isEmpty
    }

    func signIn() async throws -> (idToken: String, email: String) {
        return try await withCheckedThrowingContinuation { continuation in
            DispatchQueue.main.async {
                #if os(iOS)
                guard let presentingViewController = UIApplication.shared.connectedScenes
                    .compactMap({ $0 as? UIWindowScene })
                    .flatMap({ $0.windows })
                    .first(where: { $0.isKeyWindow })?
                    .rootViewController else {
                    continuation.resume(throwing: ErrorMessage(message: "Unable to find presenting view controller"))
                    return
                }
                GIDSignIn.sharedInstance.signIn(withPresenting: presentingViewController) { result, error in
                    GoogleSignInManager.handleResult(result: result, error: error, continuation: continuation)
                }
                #elseif os(macOS)
                guard let presentingWindow = NSApplication.shared.keyWindow else {
                    continuation.resume(throwing: ErrorMessage(message: "Unable to find presenting window"))
                    return
                }
                GIDSignIn.sharedInstance.signIn(withPresenting: presentingWindow) { result, error in
                    GoogleSignInManager.handleResult(result: result, error: error, continuation: continuation)
                }
                #endif
            }
        }
    }

    private static func handleResult(result: GIDSignInResult?, error: Error?, continuation: CheckedContinuation<(idToken: String, email: String), Error>) {
        if let error = error {
            continuation.resume(throwing: error)
            return
        }

        guard let result = result,
              let idToken = result.user.idToken?.tokenString else {
            continuation.resume(throwing: ErrorMessage(message: "Failed to get ID token from Google"))
            return
        }

        let email = result.user.profile?.email ?? JWTUtils.extractEmail(fromIdToken: idToken) ?? ""
        continuation.resume(returning: (idToken: idToken, email: email))
    }
}
#endif
