//
//  TunnelViewModel.swift
//  UpVPN
//
//  Created by Himanshu on 6/27/24.
//

import Foundation
import Combine
import WireGuardKit

@MainActor
class TunnelViewModel: ObservableObject {

    @Published var tunnelObserver = TunnelObserver(tunnelManager: TunnelManager.shared)
    private let tunnelManager = TunnelManager.shared

    private var cancellables = Set<AnyCancellable>()

    init() {
        tunnelObserver.objectWillChange.sink { [weak self] _ in
            self?.objectWillChange.send()
        }.store(in: &cancellables)
    }

    func tunnelConfig(device: Device, dns: String) {
        Task {
            var interfaceConfiguration = InterfaceConfiguration(from: device)
            interfaceConfiguration.dns = [DNSServer(from: dns)!]
            let tunnelConfiguration = TunnelConfiguration(name: "UpVPN", interface: interfaceConfiguration, peers: [])
            await self.tunnelManager.setBaseTunnelConfiguration(tunnelConfiguration: tunnelConfiguration)
        }
    }

    func reload() {
        Task {
            try await self.tunnelManager.reload()
        }
    }

    func start(to location: Location) {
        Task {
            try await self.tunnelManager.start(to: location)
        }
    }

    func stop() {
        Task {
           await self.tunnelManager.stop()
        }
    }

    func clearLastError() {
        Task {
           await self.tunnelManager.clearLastError()
        }
    }
}
