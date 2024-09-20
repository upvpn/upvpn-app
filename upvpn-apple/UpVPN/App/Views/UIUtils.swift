//
//  Utils.swift
//  UpVPN
//
//  Created by Himanshu on 8/16/24.
//

import Foundation

// from wireguard-apple TunnelViewModel.swift / MIT License
func prettyBytes(_ bytes: UInt64) -> String {
    switch bytes {
    case 0..<1024:
        return "\(bytes) B"
    case 1024 ..< (1024 * 1024):
        return String(format: "%.2f", Double(bytes) / 1024) + " KiB"
    case 1024 ..< (1024 * 1024 * 1024):
        return String(format: "%.2f", Double(bytes) / (1024 * 1024)) + " MiB"
    case 1024 ..< (1024 * 1024 * 1024 * 1024):
        return String(format: "%.2f", Double(bytes) / (1024 * 1024 * 1024)) + " GiB"
    default:
        return String(format: "%.2f", Double(bytes) / (1024 * 1024 * 1024 * 1024)) + " TiB"
    }
}
