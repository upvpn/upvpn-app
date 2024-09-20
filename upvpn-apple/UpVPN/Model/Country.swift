//
//  Country.swift
//  UpVPN
//
//  Created by Himanshu on 7/29/24.
//

import Foundation

struct Country : Identifiable, Hashable{
    var name: String
    var locations: [Location]

    var id: String {
        return self.name
    }
}
