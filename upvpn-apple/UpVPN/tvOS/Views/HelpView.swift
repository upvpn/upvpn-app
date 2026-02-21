//
//  HelpView.swift
//  UpVPN
//
//  Created by Himanshu on 9/15/24.
//

import SwiftUI

struct HelpView: View {
    var body: some View {
            Group {
                VStack {
                    Text("**What are color indicators?**")
                    Divider()
                    HStack {
                        Circle()
                            .fill(Location.WARM_COLOR)
                            .frame(width: 12, height: 12)
                        Text("Connect quickly to available servers")

                    }
                    HStack {
                        Circle()
                            .fill(Location.COLD_COLOR)
                            .frame(width: 12, height: 12)
                        Text("Create and connect to a new server")

                    }

                    Text("**Questions about product or pricing?**").padding(.top)
                    Divider()
                    Text(
                        """
                        Visit FAQ: https://UpVPN.app/faq/

                        Or email us at [support@upvpn.app](mailto:support@upvpn.app) and we'll be happy to assist!
                        """
                    )

                    Spacer()
                    Text("""
Acknowledgements: https://UpVPN.app/oss/apple/
""").font(.caption).padding()

                }
            }
            .padding()
            .multilineTextAlignment(.center)
            .navigationTitle("Help")
            .frame(maxWidth: .infinity)
        }
}

#Preview {
    HelpView()
}
