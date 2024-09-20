//
//  HelpView.swift
//  UpVPN
//
//  Created by Himanshu on 8/5/24.
//

import SwiftUI

struct HelpView: View {
    var body: some View {
            Group {
                VStack {
                    Text("""
Have questions about **product** or **pricing**?

Visit [FAQ](https://upvpn.app/faq/)

Or send us a message at [support@upvpn.app](mailto:support@upvpn.app) and we'll be happy to assist!
""")

                    Spacer()
                    Text("""
[Acknowledgements](https://upvpn.app/oss/apple/)
""").font(.caption).padding()

                }
            }
            .textSelection(.enabled)
            .padding()
            .multilineTextAlignment(.leading)
            .navigationTitle("Help")
            .frame(maxWidth: .infinity)
        }
}

#Preview {
    HelpView()
}
