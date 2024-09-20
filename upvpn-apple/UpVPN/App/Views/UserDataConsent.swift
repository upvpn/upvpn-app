//
//  UserDataConsent.swift
//  UpVPN
//
//  Created by Himanshu on 8/27/24.
//

import SwiftUI

struct UserDataConsent: View {
    var body: some View {
        CardContainer {
            Text("""
For the app to work with the UpVPN service and in-app purchases, the following device data is associated with your account:

- Unique random device ID
- Device name or hostname
- OS version and type
- CPU architecture
- WireGuard public key

**UpVPN values your privacy**. You can sign out from the device and delete your data from the UpVPN dashboard at any time.
""")
            .padding()
            .frame(minWidth: 300,  maxWidth: 500, minHeight: 300, maxHeight: .infinity)
            .multilineTextAlignment(.leading)

        }
    }
}

#Preview {
    UserDataConsent()
}
