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
                    Text("""
Have questions about **product** or **pricing**?

Visit FAQ at https://upvpn.app/faq/

Or send us a message at **support@upvpn.app** and we'll be happy to assist!
""")

                    Spacer()
                    Text("""
Acknowledgements: https://upvpn.app/oss/apple/
""").font(.caption).padding()

                }
            }
            .padding()
            .multilineTextAlignment(.leading)
            .navigationTitle("Help")
            .frame(maxWidth: .infinity)
        }
}

#Preview {
    HelpView()
}
