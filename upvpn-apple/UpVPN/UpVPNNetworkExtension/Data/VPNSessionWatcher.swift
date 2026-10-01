//
//  VPNSessionWatcher.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//

import Foundation
import os.log

class VPNSessionWatcher {
    private var request: VpnSessionStatusRequest
    private var vpnSessionRepository: VPNSessionRepository
    private var location: Location
    private var done = false
    // (status, location, isUnauthorized) where isUnauthorized is true when
    // status is .failed only because the device is no longer authorized
    private var onStatusUpdate: (VpnSessionStatus, Location, Bool) -> Void

    init(request: VpnSessionStatusRequest,
         vpnSessionRepository: VPNSessionRepository,
         location: Location,
         onStatusUpdate: @escaping (VpnSessionStatus, Location, Bool) -> Void) {
        self.request = request
        self.vpnSessionRepository = vpnSessionRepository
        self.location = location
        self.onStatusUpdate = onStatusUpdate
    }

    func watch() -> Task<Void, Never> {
        let task = Task.detached { [self] in
            os_log("watcher started")
            while !self.done && !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else { return }
                
                let result = await vpnSessionRepository.getVpnSessionStatus(request: self.request)

                var status: VpnSessionStatus
                var isUnauthorized = false
                switch result {
                case .success(let received_status):
                    status = received_status
                case .failure(let error):
                    os_log("%{public}@", "watcher error: \(error.description)")
                    if error.description == "unauthorized" {
                        status = .failed(Failed.init(
                            requestId: self.request.requestId,
                            vpnSessionUuid: self.request.vpnSessionUuid))
                        isUnauthorized = true
                    } else {
                        continue
                    }
                }

                self.onStatusUpdate(status, location, isUnauthorized)

                // if status is a terminal state end watcher
                switch status {
                case .failed, .serverReady, .clientConnected, .ended:
                    self.done = true
                default:
                    continue
                }
            }
            os_log("watcher stopped")
        }
        return task
    }
}
