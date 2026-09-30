//
//  Response.swift
//  UpVPN
//
//  Created by Himanshu on 7/5/24.
//

import Foundation

enum Response : Codable {
    case status(VPNState)
    case runtimeConfiguration(String?)

    init(data: Data) throws {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        self = try decoder.decode(Self.self, from: data)
    }

    func encode() throws -> Data {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        return try encoder.encode(self)
    }
}
