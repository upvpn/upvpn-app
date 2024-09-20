//
//  WGAdapterAsync.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//
// Portions of this are based on upstream WireGuard project
// under MIT license

import Foundation
import WireGuardKit

/// Async wrapper to original WireGuardAdapter
class WGAdapterAsync {
    private let wireguardAdapter: WireGuardAdapter

    init(wireguardAdapter: WireGuardAdapter) {
        self.wireguardAdapter = wireguardAdapter
    }

    private func log_error(adapterError: WireGuardAdapterError) {
        switch adapterError {
        case .cannotLocateTunnelFileDescriptor:
            wg_log(.error, message: "Starting tunnel failed: could not determine file descriptor")
        case .dnsResolution(let dnsErrors):
            let hostnamesWithDnsResolutionFailure = dnsErrors.map { $0.address }
                .joined(separator: ", ")
            wg_log(.error, message: "DNS resolution failed for the following hostnames: \(hostnamesWithDnsResolutionFailure)")
        case .setNetworkSettings(let error):
            wg_log(.error, message: "Starting tunnel failed with setTunnelNetworkSettings returning \(error.localizedDescription)")
        case .startWireGuardBackend(let errorCode):
            wg_log(.error, message: "Starting tunnel failed with wgTurnOn returning \(errorCode)")
        case .invalidState:
            // Must never happen
            fatalError()
        }
    }

    func start(tunnelConfiguration: TunnelConfiguration) async -> Result<Void, WireGuardAdapterError> {
        return await withCheckedContinuation { continuation in
            self.wireguardAdapter.start(tunnelConfiguration: tunnelConfiguration) { adapterError in
                if let adapterError = adapterError {
                    self.log_error(adapterError: adapterError)
                    continuation.resume(returning: .failure(adapterError))
                } else {
                    let interfaceName = self.wireguardAdapter.interfaceName ?? "unknown"
                    wg_log(.info, message: "Tunnel interface is \(interfaceName)")
                    continuation.resume(returning: .success(()))
                }
            }
        }
    }

    func stop() async -> Result<Void, WireGuardAdapterError> {
        return await withCheckedContinuation { continuation in
            self.wireguardAdapter.stop { adapterError in
                if let adapterError = adapterError {
                    wg_log(.error, message: "Failed to stop WireGuard adapter: \(adapterError.localizedDescription)")
                    continuation.resume(returning: .failure(adapterError))
                } else {
                    continuation.resume(returning: .success(()))
                }
            }
        }
    }

    func getRuntimeConfiguration() async -> String? {
        return await withCheckedContinuation { continuation in
            self.wireguardAdapter.getRuntimeConfiguration { runtimeConfiguration in
                continuation.resume(returning: runtimeConfiguration)
            }
        }
    }
}

extension WireGuardAdapterError: CustomStringConvertible {
    public var description: String {
        return switch self {
        case .cannotLocateTunnelFileDescriptor:
            "Unable to determine TUN device file descriptor"
        case .invalidState:
            // must never happen
            "fatal: invalid tunnel state"
        case .dnsResolution(_):
            "One or more domains could not be resolved"
        case .setNetworkSettings(_):
            "Unable to apply network settings to tunnel object"
        case .startWireGuardBackend(_):
            "Unable to turn on Go backend library"
        }
    }
}
