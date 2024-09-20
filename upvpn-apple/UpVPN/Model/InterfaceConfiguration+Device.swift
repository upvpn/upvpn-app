//
//  Device+InterfaceConfiguration.swift
//  UpVPN
//
//  Created by Himanshu on 8/17/24.
//

import Foundation
import WireGuardKit


extension InterfaceConfiguration {
    init(from device: Device)  {
        var interfaceConfiguration = InterfaceConfiguration(privateKey: PrivateKey(base64Key: device.privateKey)!)
        // todo: wild unwrap
        interfaceConfiguration.addresses = [IPAddressRange(from: device.ipv4Address!)!]

        self = interfaceConfiguration
    }
}
