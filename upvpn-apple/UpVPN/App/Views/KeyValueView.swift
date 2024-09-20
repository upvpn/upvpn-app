//
//  KeyValueView.swift
//  UpVPN
//
//  Created by Himanshu on 8/16/24.
//

import SwiftUI

struct KeyValueView: View {
    var key: String
    var value: String
    var body: some View {
        HStack {
            Text(key)
            Spacer()
            Text(value)
                .lineLimit(1)
                .opacity(0.5)
            #if !os(tvOS)
                .textSelection(.enabled)
            #endif
        }
    }
}

#Preview {
    KeyValueView(key: "Public key", value: "ASKJDF898SDFLKASJDBVAS9")
}
