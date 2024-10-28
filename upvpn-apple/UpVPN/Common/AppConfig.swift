//
//  AppConfig.swift
//  UpVPN
//
//  Created by Himanshu on 7/22/24.
//

import Foundation

struct AppConfig {

    static let baseURL: URL = URL(string: "https://upvpn.app/api/v1/")!
//    static let baseURL: URL = URL(string: "http://DEV/api/v1/")!

    static let version: String? = {
        Bundle.main.infoDictionary?["CFBundleVersion"] as? String
    }()

    static let shortVersionString: String? = {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    }()
}
