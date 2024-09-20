//
//  Location+Extension.swift
//  UpVPN
//
//  Created by Himanshu on 7/28/24.
//

import Foundation
import SwiftUI

extension Location {

    static let WARM_COLOR = Color(red: 22/255.0, green: 163/255.0, blue: 74/255.0, opacity: 1)
    static let COLD_COLOR = Color(red: 56/255.0, green: 189/255.0, blue: 248/255.0, opacity: 1)

    func warmOrColdColor() -> Color {
        if let estimate = self.estimate, estimate <= 10 {
           return Self.WARM_COLOR
        }

        return Self.COLD_COLOR
    }

    static func countries(from: [Location]) -> [Country] {
        var countries: [Country] = []
        var dictionary: [String: [Location]] = [:]

        for location in from {
            var newList = dictionary[location.country] ?? []
            newList.append(location)
            dictionary[location.country] = newList
        }

        for (name, locations) in dictionary {
            countries.append(Country(name: name, locations: locations))
        }

        // sort in reverse order of name
        countries.sort { (c1, c2) in
            c1.name > c2.name
        }

        return countries
    }
}
