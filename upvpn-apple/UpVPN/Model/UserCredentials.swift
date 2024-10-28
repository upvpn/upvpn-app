//
//  UserCredentials.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation

struct UserCredentials: Codable {
    var email: String
    var password: String
}

struct UserCredentialsWithCode: Codable {
    var email: String
    var password: String
    var code: String
}

struct OnlyEmail : Codable {
    var email: String
}

extension UserCredentials {
    func toUserCredentialsWithCode(code: String) -> UserCredentialsWithCode {
        .init(email: email, password: password, code: code)
    }
}
