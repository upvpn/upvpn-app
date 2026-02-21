//
//  HelpView.swift
//  UpVPN
//
//  Created by Himanshu on 8/5/24.
//

import SwiftUI

struct HelpView: View {
    var body: some View {
        VStack(alignment: .leading) {

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
                Visit [FAQ](https://upvpn.app/faq/)

                Or email us at [support@upvpn.app](mailto:support@upvpn.app) and we'll be happy to assist!
                """
            )

            Spacer()
            Text(
                """
                To delete your account, visit the [account page on the dashboard](https://upvpn.app/dashboard/account)


                [Acknowledgements](https://upvpn.app/oss/apple/)
                """
            ).font(.caption)
        }
        .multilineTextAlignment(.leading)
        .textSelection(.enabled)
        .padding()
        .padding()
        .navigationTitle("Help")
        .frame(maxWidth: .infinity)
    }
}

#Preview {
    HelpView()
}
