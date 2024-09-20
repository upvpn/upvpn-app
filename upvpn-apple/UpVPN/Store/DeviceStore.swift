//
//  DeviceStore.swift
//  UpVPN
//
//  Created by Himanshu on 7/16/24.
//

import Foundation
import WireGuardKit
import os.log

class DeviceStore {

    private static func arch() -> String {
        #if arch(x86_64)
        let arch = "x86_64"
        #elseif arch(arm64)
        let arch = "arm64"
        #else
        let arch = "unknown"
        #endif
        return arch
    }

    static func getOrInitializeDevice(name: String? = nil) async -> Result<Device, StoreError> {
        let device: Result<Device?, KeychainError> = await Keychain.get(key: StoreKeys.device.rawValue)

        switch device {
        case .success(let device):
            if let device = device {
                print("device already initalized: \(device)")
                return .success(device)
            } else {
                let hostname = ProcessInfo.processInfo.hostName.replacingOccurrences(of: ".local", with: "", options: .backwards)
                let version = "\(ProcessInfo.processInfo.operatingSystemVersion.majorVersion).\(ProcessInfo.processInfo.operatingSystemVersion.minorVersion).\(ProcessInfo.processInfo.operatingSystemVersion.patchVersion)"
                let device = Device(uniqueId: UUID(),
                                    name: name ?? hostname,
                                    version: version,
                                    arch: DeviceStore.arch(),
                                    privateKey: PrivateKey().base64Key)

                print("initializing device: \(device)")
                let result = await Keychain.upsert(key: StoreKeys.device.rawValue, item: device)

                switch result {
                case .success:
                    return .success(device)
                case .failure(let error):
                    print("failed to save device: \(error)")
                    return .failure(StoreError.keychain(error))
                }
            }
        case .failure(let error):
            print("device initialization error: \(error)")
            return .failure(StoreError.keychain(error))
        }
    }

    static func getDevice() async -> Result<Device?, StoreError> {
        return await Keychain.get(key: StoreKeys.device.rawValue)
            .mapError { StoreError.keychain($0) }
    }

    static func updateDevice(device: Device) async -> Result<(), StoreError> {
        return await Keychain.upsert(key: StoreKeys.device.rawValue, item: device)
            .mapError { StoreError.keychain($0) }
    }

    static func deleteDevice() async -> Result<(), StoreError> {
        return await Keychain.delete(key: StoreKeys.device.rawValue)
            .mapError { StoreError.keychain($0) }
    }
}
