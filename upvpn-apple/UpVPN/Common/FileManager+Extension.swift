//
//  FileManager+Extension.swift
//  UpVPN
//
//  Created by Himanshu on 7/15/24.
//
// Based on Sources/Shared/FileManager+Extension.swift
// from github.com/wireguard-apple
// Copyright WireGuard LLC / MIT License

import Foundation
import os.log

extension FileManager {
    static var appGroupId: String? {
        #if os(iOS)
        let appGroupIdInfoDictionaryKey = "app.upvpn.apple.ios.app_group_id"
        #elseif os(macOS)
        let appGroupIdInfoDictionaryKey = "app.upvpn.apple.macos.app_group_id"
        #else
        #error("Unimplemented")
        #endif
        return Bundle.main.object(forInfoDictionaryKey: appGroupIdInfoDictionaryKey) as? String
    }

    private static var sharedFolderURL: URL? {
        guard let appGroupId = FileManager.appGroupId else {
            os_log("Cannot obtain app group ID from bundle", log: OSLog.default, type: .error)
            return nil
        }
        guard let sharedFolderURL = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroupId) else {
            os_log(.error, "Cannot obtain shared folder URL")
            return nil
        }
        return sharedFolderURL
    }

    static var vpnSessionFolderURL: URL? {
        return sharedFolderURL?.appendingPathComponent("vs", isDirectory: true)
    }

    static var vpnSessionForDeletionFolderURL: URL? {
        return sharedFolderURL?.appendingPathComponent("rm", isDirectory: true)
    }

    static var networkExtensionLastErrorFileURL: URL? {
        return sharedFolderURL?.appendingPathComponent("ne-last-error.json")
    }

    static var locationsFileURL: URL? {
        return sharedFolderURL?.appendingPathComponent("locations.json")
    }

    static func deleteFile(at url: URL) -> Bool {
        do {
            try FileManager.default.removeItem(at: url)
        } catch {
            return false
        }
        return true
    }
}
