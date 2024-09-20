//
//  Device.swift
//  UpVPN
//
//  Created by Himanshu on 7/16/24.
//

import Foundation

struct Device: Codable {
    var uniqueId: UUID
    var name: String
    var version: String
    var arch: String
    var privateKey: String
    var ipv4Address: String?
    var token: String?
}

extension Device: CustomStringConvertible {
    var description: String {
        let attributes =  [
            "uniqueId: \(self.uniqueId)",
            "name: " + self.name,
            "version: " + self.version,
            "arch: " + self.arch,
            "ipv4Address: " + (self.ipv4Address ?? "nil"),
            "privateKey: <redacted>",
            "token: " + (token.map { _ in "<redacted>" } ?? "nil")
        ].joined(separator: ", ")

        return "Device(\(attributes))"
    }
}
