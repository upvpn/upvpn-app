//
//  StoreError.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation

enum StoreError: Error {
    case keychain(KeychainError)
}

extension StoreError: CustomStringConvertible {
    var description: String {
        switch self {
        case .keychain(let keychainError):
            return keychainError.description
        }
    }
}
