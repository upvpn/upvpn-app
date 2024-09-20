//
//  Location.swift
//  UpVPN
//
//  Created by Himanshu on 7/3/24.
//

import Foundation

struct Location : Codable, Hashable {
    let code: String
    let country: String
    let countryCode: String
    let city: String
    let cityCode: String
    let state: String?
    let stateCode: String?
    var estimate: UInt32?
}

extension Location: Equatable {
    static func == (lhs: Location, rhs: Location) -> Bool {
        return lhs.code == rhs.code
    }
}

extension Location: Identifiable {
    var id: String {
        self.code
    }
}

extension Location {

    static var `default`: Location {
        return Location(code: "us_va_ashburn",
                        country: "United States of America",
                        countryCode: "US",
                        city: "Ashburn",
                        cityCode: "ash",
                        state: "Virginia",
                        stateCode: "VA",
                        estimate: nil)
    }

    static var testLocation: Location {
        return Location(code: "fremont",
                        country: "United State of America",
                        countryCode: "US",
                        city: "Fremont", 
                        cityCode: "fremont",
                        state: "California",
                        stateCode: "CA",
                        estimate: 1
        )
    }

    func displayText() -> String {
        var display = self.city
        if let stateCode = self.stateCode, !stateCode.isEmpty , ["US", "CA"].contains(self.countryCode) {
            display += ", " + stateCode
        }
        return display
    }

    func matches(query: String) -> Bool {
        self.city.localizedCaseInsensitiveContains(query) ||
        self.country.localizedCaseInsensitiveContains(query) ||
        self.countryCode.localizedCaseInsensitiveContains(query) ||
        (self.state?.localizedCaseInsensitiveContains(query) ?? false) ||
        (self.stateCode?.localizedCaseInsensitiveContains(query) ?? false)
    }
}

extension Encodable {
    func asDictionary() throws -> [String: Any] {
        let data = try JSONEncoder().encode(self)
        guard let dictionary = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any] else {
            throw NSError()
        }
        return dictionary
    }
}

extension Decodable {
    init(from dictionary: [String: Any]) throws {
        let data = try JSONSerialization.data(withJSONObject: dictionary, options: [])
        self = try JSONDecoder().decode(Self.self, from: data)
    }
}
