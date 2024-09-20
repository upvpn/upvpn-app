//
//  PacketTunnelProvider.swift
//  UpVPNNetworkExtension
//
//  Created by Himanshu on 6/20/24.
//

import NetworkExtension
import os.log
import WireGuardKit

func wg_log(_ type: OSLogType, message msg: String) {
    os_log("%{public}s", log: OSLog.default, type: type, msg)
}

class PacketTunnelProvider: NEPacketTunnelProvider {

    var vpnOrchestrator: VPNOrchestrator!

    override init() {
        os_log("init PacketTunnelProvider")
        super.init()

        defer {
            self.vpnOrchestrator = VPNOrchestrator(packetTunnelProvider: self)
        }
    }

    override func startTunnel(options: [String : NSObject]?, completionHandler: @escaping (Error?) -> Void) {
        let startRequestId = options?["startRequestId"] as? String
        let location = (try? Location(from: options ?? [:])) ?? Location.default

        os_log("%{public}@", "staring tunnel from " + (startRequestId == nil ? "OS" : "app") +  " to location " + location.city)

        Task {
            let errorNotifier = await ErrorNotifier(startRequestId: startRequestId)
            let result = await self.vpnOrchestrator.startAndWait(location: location)

            // todo log outcome?
            switch result {
            case .success():
                completionHandler(nil)
            case .failure(let orcaError):
                await errorNotifier.notify(orcaError)
                completionHandler(orcaError)
            }
        }
    }

    override func stopTunnel(with reason: NEProviderStopReason, completionHandler: @escaping () -> Void) {
        os_log("stopTunnel")

        Task {
            // todo return error and handle error
            let _ = await self.vpnOrchestrator.sendCommand(Stop(reason: "client requested"))
           completionHandler()

            // From upstream WireGuard project / MIT license
            #if os(macOS)
            // HACK: This is a filthy hack to work around Apple bug 32073323 (dup'd by us as 47526107).
            // Remove it when they finally fix this upstream and the fix has been rolled out to
            // sufficient quantities of users.
            exit(0)
            #endif
        }
    }

    override func handleAppMessage(_ messageData: Data, completionHandler: ((Data?) -> Void)?) {
        if let handler = completionHandler {
            if let request = try? Request(data: messageData) {
                switch request {
                case .status:
                    Task {
                        let vpnState = await self.vpnOrchestrator.getStatus()
                        let response = try? Response.status(vpnState).encode()
                        handler(response)
                    }
                case .getRuntimeConfiguration:
                    Task {
                        let runtimeConfiguration = await self.vpnOrchestrator.getRuntimeConfiguration()
                        let response = try? Response.runtimeConfiguration(runtimeConfiguration).encode()
                        handler(response)
                    }
                }
            } else {
                handler(nil)
            }
        }
    }

    override func sleep(completionHandler: @escaping () -> Void) {
        os_log("sleep")
        completionHandler()
    }

    override func wake() {
        os_log("wake")
    }
}
