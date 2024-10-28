//
//  VPNSessionStore.swift
//  UpVPN
//
//  Created by Himanshu on 7/24/24.
//

import Foundation
import os.log

class VPNSessionStore {

    static var shared = VPNSessionStore()

    private static func url(_ requestId: UUID) -> URL? {
        FileManager.vpnSessionFolderURL?.appendingPathComponent(requestId.uuidString)
    }

    private static func urlForDeletion(_ requestId: UUID) -> URL? {
        FileManager.vpnSessionForDeletionFolderURL?.appendingPathComponent(requestId.uuidString)
    }

    private init() {
        Task {
            if let vpnSessionFolderURL = FileManager.vpnSessionFolderURL {
                try? FileManager.default.createDirectory(at: vpnSessionFolderURL, withIntermediateDirectories: true)
            }

            if let vpnSessionForDeletionFolderURL = FileManager.vpnSessionForDeletionFolderURL {
                try? FileManager.default.createDirectory(at: vpnSessionForDeletionFolderURL, withIntermediateDirectories: true)
            }
        }
    }

    func save(requestId: UUID) async {
        await Task {
            if let url = Self.url(requestId) {
                FileManager.default.createFile(atPath: url.path, contents: nil)
            }
        }.value
    }

    func delete(requestId: UUID) async {
        await Task {
            if let urlForDeletion = Self.urlForDeletion(requestId) {
                try? FileManager.default.removeItem(at: urlForDeletion)
            }

            if let url = Self.url(requestId) {
                try? FileManager.default.removeItem(at: url)
            }
        }.value
    }

    func markForDeletion(requestId: UUID) async {
        await Task {
            if let at = Self.url(requestId), let to = Self.urlForDeletion(requestId) {
                try? FileManager.default.moveItem(at: at, to: to)
                os_log("%{public}@", "marked \(requestId.uuidString) for deletion")
            }
        }.value
    }

    func markAllForDeletion() {
         Task {
            if let vpnSessionFolderURL = FileManager.vpnSessionFolderURL {
                let items = try? FileManager.default.contentsOfDirectory(at: vpnSessionFolderURL, includingPropertiesForKeys: nil)

                if let items = items {
                    for item in items {
                        let requestIdString = item.lastPathComponent
                        if !requestIdString.isEmpty, let requestId = UUID(uuidString: requestIdString) {
                            await self.markForDeletion(requestId: requestId)
                        }
                    }
                }
            }
         }
    }

    func sessionsToReclaim() async -> [UUID] {
        return await Task {
            var requestIds = []
            if let vpnSessionForDeletionFolderURL = FileManager.vpnSessionForDeletionFolderURL {
                let items = try? FileManager.default.contentsOfDirectory(at: vpnSessionForDeletionFolderURL, includingPropertiesForKeys: nil)

                if let items = items {
                    for item in items {
                        let requestIdString = item.lastPathComponent
                        if !requestIdString.isEmpty, let requestId = UUID(uuidString: requestIdString) {
                            requestIds.append(requestId)
                        }
                    }
                }
            }

            os_log("%{public}@", "total sessions to reclaim: \(requestIds.count)")
            return requestIds
        }.value as? [UUID] ?? []
    }

}
