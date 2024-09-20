//
//  Request.swift
//  UpVPN
//
//  Created by Himanshu on 7/5/24.
//

import Foundation

enum Request : Codable {
    case status
    case getRuntimeConfiguration

    init(data: Data) throws {
        self = try JSONDecoder().decode(Self.self, from: data)
    }

    public func encode() throws -> Data {
        try JSONEncoder().encode(self)
    }
}
