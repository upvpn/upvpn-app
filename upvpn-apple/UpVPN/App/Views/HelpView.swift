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
            Text("""
Have questions about **product** or **pricing**?

Visit [FAQ](https://upvpn.app/faq/)

Or email us at [support@upvpn.app](mailto:support@upvpn.app) and we'll be happy to assist!
""")

                Spacer()
                Text("""
To delete your account, visit the [account page on the dashboard](https://upvpn.app/dashboard/account)


[Acknowledgements](https://upvpn.app/oss/apple/)
""").font(.caption)
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
