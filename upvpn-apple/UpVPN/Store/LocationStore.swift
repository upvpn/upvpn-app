//
//  LocationStore.swift
//  UpVPN
//
//  Created by Himanshu on 7/24/24.
//

import Foundation

class LocationStore {

    static var MAX_RECENT = 5
    private var userDefaults = UserDefaults()

    static func save(locations: [Location]) async throws {
        let task = Task {
            let data = try JSONEncoder().encode(locations)
            guard let file = FileManager.locationsFileURL else {
                return
            }
            try data.write(to: file)
        }
        _ = try await task.value
    }

    static func load() async throws -> [Location] {
        let task = Task<[Location], Error> {
            guard let file = FileManager.locationsFileURL else {
                // todo error?
                return []
            }

            guard let data = try? Data(contentsOf: file) else {
                return []
            }
            let locations = try JSONDecoder().decode([Location].self, from: data)
            return locations
        }
        return try await task.value
    }

    // UserDefault API usage requires privacy manifest (PrivacyInfo.xcprivacy):
    // https://developer.apple.com/documentation/bundleresources/privacy_manifest_files/describing_use_of_required_reason_api
    func saveRecent(code: String) async {
        // order of storage is most recent is in the end of the list
        await Task {
            var previous = self.userDefaults.stringArray(forKey: "recent")?.suffix(Self.MAX_RECENT - 1) ?? []
            if let index = previous.firstIndex(of: code) {
                previous.remove(at: index)
            }
            previous.append(code)
            self.userDefaults.set(previous, forKey: "recent")
        }.value
    }

    func loadRecent() async -> [String] {
        return await Task {
            return self.userDefaults.stringArray(forKey: "recent") ?? []
        }.value
    }
}
