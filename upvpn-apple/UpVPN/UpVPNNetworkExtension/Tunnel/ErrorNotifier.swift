//
//  ErrorNotifier.swift
//  UpVPN
//
//  Created by Himanshu on 7/15/24.
//
// Based on Sources/WireGuardNetworkExtension/ErrorNotifier.swift
// from github.com/wireguard-apple
// Copyright WireGuard LLC / MIT License


import Foundation

class ErrorNotifier {
    let startRequestId: String?

    init(startRequestId: String?) async {
        self.startRequestId = startRequestId
        await ErrorNotifier.removeLastErrorFile()
    }

    func notify(_ error: OrchestratorError) async {
        let task = Task {
            guard let startRequestId = startRequestId, let lastErrorFilePath = FileManager.networkExtensionLastErrorFileURL?.path else { return }
            let errorMessageData = "\(startRequestId)\n\(error)".data(using: .utf8)
            FileManager.default.createFile(atPath: lastErrorFilePath, contents: errorMessageData, attributes: nil)
        }
        await task.value
    }

    static func removeLastErrorFile() async {
        let task = Task {
            if let lastErrorFileURL = FileManager.networkExtensionLastErrorFileURL {
                _ = FileManager.deleteFile(at: lastErrorFileURL)
            }
        }
        await task.value
    }
}
