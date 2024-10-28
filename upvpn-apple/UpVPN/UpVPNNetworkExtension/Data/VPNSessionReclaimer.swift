//
//  VPNSessionReclaimer.swift
//  UpVPN
//
//  Created by Himanshu on 7/24/24.
//

import Foundation
import os.log

class VPNSessionReclaimer {
    func run(deviceUniqueId: UUID, vpnApiService: VPNApiService) async {
        let toReclaim = await VPNSessionStore.shared.sessionsToReclaim()

        if toReclaim.isEmpty {
            return
        }

        for requestId in toReclaim {
            let request = EndSessionApi(requestId: requestId, deviceUniqueId: deviceUniqueId, reason: "reclaimed")
            let result = await vpnApiService.endVpnSession(request: request)

            if case .success = result {
                os_log("%{public}@", "reclaimed \(requestId.uuidString)")
                await VPNSessionStore.shared.delete(requestId: requestId)
            }
        }
    }
}
