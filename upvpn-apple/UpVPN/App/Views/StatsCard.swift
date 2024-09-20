//
//  StatsCard.swift
//  UpVPN
//
//  Created by Himanshu on 7/30/24.
//

import SwiftUI

struct StatsCard: View {

    var tx: String
    var rx: String

    var body: some View {
        HStack() {
            Stat(systemName: "arrow.down.circle", text: rx, caption: "DOWNLOADED")
            Divider()
                .padding()
            Stat(systemName: "arrow.up.circle", text: tx, caption: "UPLOADED")
        }
    }
}

struct Stat: View {
    var systemName: String
    var text: String
    var caption: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: systemName)
                .resizable()
                .scaledToFit()
                .frame(width: 40, height: 40)

            VStack(alignment: .leading, spacing: 5) {
                Text(text)
                    .font(.headline)
                Text(caption)
                    .font(.caption)
                    .fontWeight(.regular)
                    .opacity(0.5)
            }
        }
    }
}

#Preview {
    StatsCard(tx: "10 KiB", rx: "100 GiB")
}
