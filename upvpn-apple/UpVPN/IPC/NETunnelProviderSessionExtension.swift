//
//  NETunnelProviderSessionExtension.swift
//  UpVPN
//
//  Created by Himanshu on 7/9/24.
//

import Foundation
import NetworkExtension

extension NETunnelProviderSession {
    func asyncSendProviderMessage(data: Data) async throws -> Data? {
        return try await withCheckedThrowingContinuation { continuation in
            do {
                try self.sendProviderMessage(data) { responseData in
                        continuation.resume(returning: responseData)
                }
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }

    func sendProviderMessage(request: Request) async throws -> Response? {
        let responseData = try await asyncSendProviderMessage(data: request.encode())
        if let responseData = responseData {
            return try Response(data: responseData)
        }
        return nil
    }
}
