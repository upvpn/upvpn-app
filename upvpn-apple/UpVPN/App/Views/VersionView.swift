//
//  VersionView.swift
//  UpVPN
//
//  Created by Himanshu on 8/16/24.
//

import SwiftUI

struct VersionView: View {
    var body: some View {
        HStack(spacing: 2) {
            if let shortVersionString = AppConfig.shortVersionString {
                Text(shortVersionString)
            }
            if let version = AppConfig.version {
                Text("(\(version))")
            }
        }
    }
}

#Preview {
    VersionView()
}
