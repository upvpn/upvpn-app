//
//  UserStore.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation

class UserStore {
    static func getCredentials() async -> Result<UserCredentials?, StoreError> {
        return await Keychain.get(key: StoreKeys.userCredentials.rawValue)
            .mapError{ StoreError.keychain($0) }
    }

    static func saveCredentials(userCredentials: UserCredentials) async -> Result<(), StoreError> {
        return await Keychain.upsert(key: StoreKeys.userCredentials.rawValue, item: userCredentials)
            .mapError { StoreError.keychain($0) }
    }
}
