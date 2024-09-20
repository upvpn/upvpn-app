//
//  RuntimeConfigurationView.swift
//  UpVPN
//
//  Created by Himanshu on 8/15/24.
//

import SwiftUI
import WireGuardKit

struct RuntimeConfigurationView: View {

    @EnvironmentObject var tunnelViewModel: TunnelViewModel

    private var tc: TunnelConfiguration? {
        return tunnelViewModel.tunnelObserver.runtimeConfig
    }

    var body: some View {
        List {
            Section("Interface") {
                KeyValueView(key: "Public key", value: tc?.interface.privateKey.publicKey.base64Key ?? "-")
                KeyValueView(key: "Addresses", value: tc?.interface.addresses
                        .map({ip in ip.stringRepresentation})
                        .joined(separator: ",") ?? "-")
                KeyValueView(key: "Listen port", value: tc?.interface.listenPort?.description ?? "-")
                KeyValueView(key: "DNS servers", value: tc?.interface.dns
                    .map({ dns in dns.stringRepresentation})
                    .joined(separator: ",") ?? "-")
            }

            ForEach(tc?.peers ?? [], id: \.publicKey) { peer in
                Section("Peer") {
                    KeyValueView(key: "Public key", value: peer.publicKey.base64Key)
                    KeyValueView(key: "Endpoint", value: peer.endpoint?.stringRepresentation ?? "-")
                    KeyValueView(key: "Allowed IPs", value: peer.allowedIPs
                        .map({ ip in ip.stringRepresentation})
                        .joined(separator: ","))
                    KeyValueView(key: "Persistent keepalive", value: peer.persistentKeepAlive?.description ?? "-")
                    KeyValueView(key: "Data Sent", value:  prettyBytes(peer.txBytes ?? 0) )
                    KeyValueView(key: "Data Received", value: prettyBytes(peer.rxBytes ?? 0))

                    if let lastHandshakeTime = peer.lastHandshakeTime {
                        KeyValueView(key: "Latest handshake",
                                     value: RelativeDateTimeFormatter()
                                            .localizedString(for: lastHandshakeTime, relativeTo: Date()))
                    }
                }
                #if os(tvOS)
                // so that list can scroll on tvOS
                .focusable()
                #endif
                // otherwise list does not update
                .onReceive(tunnelViewModel.tunnelObserver.$runtimeConfig) { _ in  }
            }
        }
        #if os(iOS)
        // only available on iOS
        .modifier(ListStyleModifer())
        #endif
    }
}

#if os(iOS)
struct ListStyleModifer : ViewModifier {
    
    func body(content: Content) -> some View {
        if UIDevice.current.userInterfaceIdiom == .pad {
            content
                .listStyle(.insetGrouped)
        } else {
            content
                .listStyle(.grouped)
        }
    }
}
#endif

#Preview {
    RuntimeConfigurationView()
        .environmentObject(TunnelViewModel())
}
