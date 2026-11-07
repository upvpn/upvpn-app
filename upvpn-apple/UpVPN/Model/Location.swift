//
//  Location.swift
//  UpVPN
//
//  Created by Himanshu on 7/3/24.
//

import Foundation

/// Who pays for a session at a location.
///
/// - `free`: never charged, subject to the free usage limits
/// - `standard`: covered by a yearly plan, otherwise billed from the pay as you go balance
/// - `premium`: always billed from the pay as you go balance, whatever the plan
enum LocationTier: String, Codable, CaseIterable, Hashable {
    case free
    case standard
    case premium

    /// Lenient: a tier the API adds later decodes as `standard` instead of failing the whole list.
    init(from decoder: Decoder) throws {
        let raw = try decoder.singleValueContainer().decode(String.self)
        self = LocationTier(rawValue: raw) ?? .standard
    }

    var displayName: String {
        switch self {
        case .free: "Free"
        case .standard: "Standard"
        case .premium: "Premium"
        }
    }
}

/// The current price of a location, in cents.
struct Price: Codable, Hashable {
    var perGb: Int32
    var perHour: Int32
    var tier: LocationTier
}

/// The geographical pin of a location.
struct Geo: Codable, Hashable {
    var latitude: Double
    var longitude: Double
}

/// `/api/v2/locations` response: an object rather than a bare list so that fields can be
/// added next to `locations` later without changing the shape.
struct LocationsV2: Decodable {
    var locations: [Location]
}

/// A location from `/api/v2/locations`.
///
/// Every field the v2 API added over v1 is optional or defaulted when decoding, so a location
/// saved by an older version of the app (locations file, tunnel options) still decodes.
/// Unknown keys are ignored by `JSONDecoder`, so the API can grow freely.
struct Location : Codable, Hashable {
    let code: String
    let country: String
    let countryCode: String
    let city: String
    let cityCode: String
    let state: String?
    let stateCode: String?
    var estimate: UInt32?
    var price: Price? = nil
    var geo: Geo? = nil
    /// one of the locations the app picks from when the user has not chosen one yet
    var isDefault: Bool = false
}

extension Location {
    enum CodingKeys: String, CodingKey {
        case code, country, countryCode, city, cityCode, state, stateCode, estimate, price, geo, isDefault
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        code = try container.decode(String.self, forKey: .code)
        country = try container.decode(String.self, forKey: .country)
        countryCode = try container.decode(String.self, forKey: .countryCode)
        city = try container.decode(String.self, forKey: .city)
        cityCode = try container.decode(String.self, forKey: .cityCode)
        state = try container.decodeIfPresent(String.self, forKey: .state)
        stateCode = try container.decodeIfPresent(String.self, forKey: .stateCode)
        estimate = try container.decodeIfPresent(UInt32.self, forKey: .estimate)
        // v2 fields: absent in data saved by older versions of the app, and a value this
        // version cannot read must not take the whole location down with it
        price = try? container.decodeIfPresent(Price.self, forKey: .price)
        geo = try? container.decodeIfPresent(Geo.self, forKey: .geo)
        isDefault = (try? container.decodeIfPresent(Bool.self, forKey: .isDefault)) ?? false
    }
}

extension Location: Equatable {
    static func == (lhs: Location, rhs: Location) -> Bool {
        return lhs.code == rhs.code
    }

    // consistent with ==, which identifies a location by its code
    func hash(into hasher: inout Hasher) {
        hasher.combine(self.code)
    }
}

extension Location: Identifiable {
    var id: String {
        self.code
    }
}

extension Location {

    var tier: LocationTier? {
        self.price?.tier
    }

    var isPremium: Bool {
        self.tier == .premium
    }

    var isFree: Bool {
        self.tier == .free
    }

    /// Only for the network extension started by the OS without a location, and for previews.
    /// Never a fallback for the location selection in the app.
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
                        estimate: 1,
                        price: Price(perGb: 100, perHour: 1, tier: .premium)
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

/// A filter of locations by tier, as offered next to search.
enum LocationTierFilter: Hashable, Identifiable {
    case all
    case tier(LocationTier)

    var id: Self { self }

    var label: String {
        switch self {
        case .all: "All"
        case .tier(let tier): tier.displayName
        }
    }

    func matches(_ location: Location) -> Bool {
        switch self {
        case .all: true
        case .tier(let tier): location.tier == tier
        }
    }
}

extension Array where Element == Location {

    /// The location to select when the user has not chosen one yet: any of the locations the
    /// API flags as default, at random when there are several; the first location when the
    /// list has no default.
    func defaultLocation() -> Location? {
        self.filter { $0.isDefault }.randomElement() ?? self.first
    }

    /// The tiers present in this list, in display order, so that a filter offers only those.
    var availableTiers: [LocationTier] {
        LocationTier.allCases.filter { tier in self.contains { $0.tier == tier } }
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
