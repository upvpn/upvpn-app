//
//  Keychain.swift
//  UpVPN
//
//  Created by Himanshu on 7/16/24.
//

import Foundation
import Security

enum KeychainError: Error {
    case unhandledError(status: OSStatus)
    case codingError(isEncoding: Bool, error: Error)
}

extension KeychainError: CustomStringConvertible {
    var description: String {
        switch self {
        case .unhandledError(let status):
            return status.description
        case .codingError(let isEncoding, let error):
            return "\(isEncoding ? "encoding" : "decoding") error \(error)"
        }
    }
}

class Keychain {

    /// App Group for sharing Keychain items by setting kSecUseDataProtectionKeychain:true (without the password prompt to grant access to NE) only works on iOS
    /// but on macOS you get errSecMissingEntitlement even though app an appex runs as same user.
    /// More info: https://developer.apple.com/forums/thread/133677?answerId=422887022#422887022
    /// Hence on macOS we use keychain access group instead of app group
    static var keychainGroupId: String? {
        #if os(iOS) || os(tvOS)
        return FileManager.appGroupId
        #elseif os(macOS)
        let keychainGroupIdInfoDictionaryKey = "app.upvpn.apple.macos.keychain_group_id"
        return Bundle.main.object(forInfoDictionaryKey: keychainGroupIdInfoDictionaryKey) as? String
        #else
        #error("Unimplemented")
        #endif
    }

    // todo have insert and update as separate methods?
    static func upsert<T: Codable>(key: String, item: T) async -> Result<(), KeychainError> {
        await Task {
            do {
                // todo fix forced unwrap of keychainGroupId
                // todo extract common attributes in all the functions here
                let data = try JSONEncoder().encode(item)
                let label = "UpVPN: \(key)"
                let query: [String: Any] = [
                    kSecClass as String: kSecClassGenericPassword,
                    kSecAttrLabel as String: label,
                    kSecAttrAccount as String: key,
                    kSecAttrService as String: bundleIdentifireWithoutNE(),
                    kSecAttrSynchronizable as String: false,
                    kSecAttrAccessGroup as String: Keychain.keychainGroupId!,
                    kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
                    kSecValueData as String: data,
                    kSecUseDataProtectionKeychain as String: true,
                ]

                let status = SecItemAdd(query as CFDictionary, nil)

                if status == errSecDuplicateItem {
                    let query: [String: Any] = [
                        kSecClass as String: kSecClassGenericPassword,
                        kSecAttrLabel as String: label,
                        kSecAttrAccount as String: key,
                        kSecAttrService as String: bundleIdentifireWithoutNE(),
                        kSecAttrSynchronizable as String: false,
                        kSecAttrAccessGroup as String: Keychain.keychainGroupId!,
                        kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
                        kSecUseDataProtectionKeychain as String: true,
                    ]

                    let attributesToUpdate: [String: Any] = [kSecValueData as String: data]

                    let updateStatus = SecItemUpdate(query as CFDictionary, attributesToUpdate as CFDictionary)

                    guard updateStatus == errSecSuccess else {
                        return .failure(.unhandledError(status: updateStatus))
                    }
                } else if status != errSecSuccess {
                    return .failure(.unhandledError(status: status))
                }

                return .success(())
            } catch {
                return .failure(.codingError(isEncoding: true, error: error))
            }
        }.value
    }

    static func get<T: Codable>(key: String) async -> Result<T?, KeychainError> {
        await Task {
            let query: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: key,
                kSecAttrService as String: bundleIdentifireWithoutNE(),
                kSecAttrSynchronizable as String: false,
                kSecAttrAccessGroup as String: Keychain.keychainGroupId!,
                kSecUseDataProtectionKeychain as String: true,
                kSecMatchLimit as String: kSecMatchLimitOne,
                kSecReturnData as String: true
            ]

            var item: CFTypeRef?
            let status = SecItemCopyMatching(query as CFDictionary, &item)

            guard status == errSecSuccess else {
                if status == errSecItemNotFound { return .success(nil) }
                return .failure(.unhandledError(status: status))
            }

            guard let data = item as? Data else { return .success(nil) }

            do {
                let decodedItem = try JSONDecoder().decode(T.self, from: data)
                return .success(decodedItem)
            } catch {
                return .failure(.codingError(isEncoding: false, error: error))
            }
        }.value
    }

    static func delete(key: String) async -> Result<(), KeychainError> {
        await Task {
            let query: [String: Any] = [
                kSecClass as String: kSecClassGenericPassword,
                kSecAttrAccount as String: key,
                kSecAttrService as String: bundleIdentifireWithoutNE(),
                kSecAttrSynchronizable as String: false,
                kSecAttrAccessGroup as String: Keychain.keychainGroupId!,
                kSecUseDataProtectionKeychain as String: true,
                // todo: if there are multiple items for the key this limit makes it fail
//                kSecMatchLimit as String: kSecMatchLimitOne
            ]

            let status = SecItemDelete(query as CFDictionary)

            guard status == errSecSuccess || status == errSecItemNotFound else {
                return .failure(.unhandledError(status: status))
            }

            return .success(())
        }.value
    }
}
