//
//  TunnelConfigurationExtension.swift
//  UpVPN
//
//  Created by Himanshu on 7/23/24.
//

import Foundation
import WireGuardKit

extension TunnelConfiguration {
    static func from(serverReady: ServerReady, interface: InterfaceConfiguration) -> TunnelConfiguration {
        // todo: wild unwrap, todo: allowed IPs?
        var peer = PeerConfiguration(publicKey: PublicKey(base64Key: serverReady.publicKey)!)
        peer.endpoint = Endpoint(from: serverReady.ipv4Endpoint)
        peer.allowedIPs = [IPAddressRange(from: "0.0.0.0/0")!, IPAddressRange(from: "::/0")!]
        peer.persistentKeepAlive = 25

        return TunnelConfiguration(name: "UpVPN", interface: interface, peers: [peer])
    }
}
