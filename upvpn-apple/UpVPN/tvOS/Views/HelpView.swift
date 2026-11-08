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
                    Text("**What do the indicators and tiers mean?**")
                    Divider()
                    
                    HStack {
                        Circle()
                            .fill(Location.WARM_COLOR)
                            .frame(width: 20, height: 20)
                        Text("Connect quickly to available servers")
                        
                    }
                    HStack {
                        Circle()
                            .fill(Location.COLD_COLOR)
                            .frame(width: 20, height: 20)
                        Text("Create and connect to a new server")
                        
                    }
                    HStack(alignment: .top) {
                        Text("💰")
                            .frame(width: 20, height: 20)
                        Text("Premium location, costs higher")
                    }

                    Text("**Questions about product or pricing?**").padding(.top)
                    Divider()
                    Text(
                        """
                        Visit FAQ: https://UpVPN.app/faq/

                        Estimate cost of a location: https://UpVPN.app/pricing-calculator/

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
