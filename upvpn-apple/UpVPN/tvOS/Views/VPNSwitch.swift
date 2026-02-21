//
//  VPNSwitch.swift
//  UpVPN
//
//  Created by Himanshu on 9/14/24.
//

import SwiftUI

struct VPNSwitch: View {
    var tunnelStatus: TunnelStatus
    var start: () -> Void = {}
    var stop: () -> Void = {}

    private var isOnBinding: Binding<Bool> {
            Binding(
                get: { self.tunnelStatus.shouldToggleBeOn() },
                set: { newValue in
                    if newValue != self.tunnelStatus.shouldToggleBeOn() {
                        if newValue {
                            self.start()
                        } else {
                            self.stop()
                        }
                    }
                }
            )
        }


    var body: some View {
        Toggle(isOn: isOnBinding) {
            HStack(spacing: 15) {
                Text(self.tunnelStatus.displayText())
                if !self.tunnelStatus.isDisconnectedOrConnected() {
                    ProgressView()
                }
            }
        }
        .disabled(!tunnelStatus.isDisconnectedOrConnected())
        .background(Color.clear)
        .frame( maxWidth: .infinity)
    }
}
