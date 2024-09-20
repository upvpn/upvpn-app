//
//  Types.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation
import WireGuardKit

enum DeviceType: String, Codable {
    case macOS = "macos"
    case ios = "ios"

    static var current: DeviceType {
        #if os(macOS)
        return DeviceType.macOS
        #else
        return DeviceType.ios
        #endif
    }
}


struct DeviceInfo: Codable {
    var name: String
    var version: String
    var arch: String
    var publicKey: String
    var uniqueId: UUID
    var deviceType: DeviceType

    init(from: Device) {
        name = from.name
        version = from.version
        arch = from.arch
        publicKey = PrivateKey(base64Key: from.privateKey)!.publicKey.base64Key
        uniqueId = from.uniqueId
        deviceType = DeviceType.current
    }
}

struct AddDeviceRequest: Codable {
    var userCreds: UserCredentials
    var deviceInfo: DeviceInfo
}

struct DeviceAddresses: Codable {
    var ipv4Address: String
}

struct AddDeviceResponse: Codable {
    var token: String
    var deviceAddresses: DeviceAddresses
}

struct Empty: Codable {}

struct NewSession : Codable {
    var requestId: UUID
    var deviceUniqueId: UUID
    var locationCode: String
}


struct EndSessionApi : Codable {
    var requestId: UUID
    var deviceUniqueId: UUID
    var vpnSessionUuid: UUID?
    var reason: String
}

struct Accepted : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
}

struct ServerCreated : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
}

struct ServerRunning : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
}

struct Failed : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
}

struct ServerReady : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
    var publicKey: String
    var ipv4Endpoint: String // todo: native ipv4 data type
    var privateIpv4: String // todo: ip network address type
}

struct ClientConnected: Codable {
    var requestId: UUID
    var deviceUniqueId: UUID
    var vpnSessionUuid: UUID
}

struct Ended : Codable {
    var requestId: UUID
    var vpnSessionUuid: UUID
}

struct SessionMeta {
    var requestId: UUID
    var vpnSessionUuid: UUID?
}

struct VpnSessionStatusRequest : Codable {
    var requestId: UUID
    var deviceUniqueId: UUID
    var vpnSessionUuid: UUID
}

enum VpnSessionStatus: Codable {
    case accepted(Accepted)
    case failed(Failed)
    case serverCreated(ServerCreated)
    case serverRunning(ServerRunning)
    case serverReady(ServerReady)
    case clientConnected(ClientConnected)
    case ended(Ended)

    enum CodingKeys: String, CodingKey {
        case type
        case content
    }

    enum StatusType: String, Codable {
        case accepted
        case failed
        case serverCreated
        case serverRunning
        case serverReady
        case clientConnected
        case ended
    }

    init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let type = try container.decode(StatusType.self, forKey: .type)

        switch type {
        case .accepted:
            let accepted = try container.decode(Accepted.self, forKey: .content)
            self = .accepted(accepted)
        case .failed:
            let failed = try container.decode(Failed.self, forKey: .content)
            self = .failed(failed)
        case .serverCreated:
            let serverCreated = try container.decode(ServerCreated.self, forKey: .content)
            self = .serverCreated(serverCreated)
        case .serverRunning:
            let serverRunning = try container.decode(ServerRunning.self, forKey: .content)
            self = .serverRunning(serverRunning)
        case .serverReady:
            let serverReady = try container.decode(ServerReady.self, forKey: .content)
            self = .serverReady(serverReady)
        case .clientConnected:
            let clientConnected = try container.decode(ClientConnected.self, forKey: .content)
            self = .clientConnected(clientConnected)
        case .ended:
            let ended = try container.decode(Ended.self, forKey: .content)
            self = .ended(ended)
        }
    }

    func encode(to encoder: any Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        switch self {
        case .accepted(let accepted):
            try container.encode(StatusType.accepted, forKey: .type)
            try container.encode(accepted, forKey: .content)
        case .failed(let failed):
            try container.encode(StatusType.failed, forKey: .type)
            try container.encode(failed, forKey: .content)
        case .serverCreated(let serverCreated):
            try container.encode(StatusType.serverCreated, forKey: .type)
            try container.encode(serverCreated, forKey: .content)
        case .serverRunning(let serverRunning):
            try container.encode(StatusType.serverRunning, forKey: .type)
            try container.encode(serverRunning, forKey: .content)
        case .serverReady(let serverReady):
            try container.encode(StatusType.serverReady, forKey: .type)
            try container.encode(serverReady, forKey: .content)
        case .clientConnected(let clientConnected):
            try container.encode(StatusType.clientConnected, forKey: .type)
            try container.encode(clientConnected, forKey: .content)
        case .ended(let ended):
            try container.encode(StatusType.ended, forKey: .type)
            try container.encode(ended, forKey: .content)
        }
    }
}

struct UserPlanPayAsYouGo: Codable {
    var balance: Int32
}

enum UserPlan: Decodable {
    case PayAsYouGo(UserPlanPayAsYouGo)
    case AnnualSubscription

    enum CodingKeys: String, CodingKey {
        case type
        case content
    }

    enum UserPlanType: String, Codable {
        case PayAsYouGo
        case AnnualSubscription
    }


    init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let type = try container.decode(UserPlanType.self, forKey: .type)

        switch type {
        case .PayAsYouGo:
            let accepted = try container.decode(UserPlanPayAsYouGo.self, forKey: .content)
            self = .PayAsYouGo(accepted)
        case .AnnualSubscription:
            self = .AnnualSubscription
        }
    }
}
