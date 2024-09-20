//
//  TunnelObserver.swift
//  UpVPN
//
//  Created by Himanshu on 7/9/24.
//

import Foundation
import WireGuardKit

@MainActor
class TunnelObserver: ObservableObject {
    @MainActor @Published var tunnelStatus: TunnelStatus = TunnelStatus.loading
    @MainActor @Published var lastError: String? = nil
    @MainActor @Published var runtimeConfig: TunnelConfiguration? = nil

    private var task: Task<Void, Never>? = nil

    init(tunnelManager: TunnelManager) {
        self.task = Task {
            await withTaskGroup(of: Void.self) { group in
                group.addTask {
                    for await tunnelStatus in await tunnelManager.$tunnelStatus.values {
                        if Task.isCancelled { break }
                        await MainActor.run {
                            self.tunnelStatus = tunnelStatus
                        }
                    }
                }

                group.addTask {
                    for await lastError in await tunnelManager.$lastError.values {
                        if Task.isCancelled { break }
                        await MainActor.run {
                            self.lastError = lastError
                        }
                    }
                }

                group.addTask {
                    for await runtimeConfig in await tunnelManager.$runtimeConfig.values {
                        if Task.isCancelled { break }
                        await MainActor.run {
                            self.runtimeConfig = runtimeConfig
                        }
                    }
                }

                await group.waitForAll()
            }
        }
    }

    deinit {
        self.task?.cancel()
    }
}
