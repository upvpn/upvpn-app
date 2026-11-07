//
//  HelpView.swift
//  UpVPN
//
//  Created by Himanshu on 8/5/24.
//

import SwiftUI
#if os(iOS)
import SafariServices
#elseif os(macOS)
import WebKit
#endif

/// A web page to show inside the app.
struct WebPage: Identifiable {
    let url: URL
    var id: String { url.absoluteString }
}

struct HelpView: View {

    /// the web page open in the app, if any: every http(s) link on this page opens here
    @State private var presentedPage: WebPage? = nil

    var body: some View {
        VStack(alignment: .leading) {

            Text("**What do indicators mean?**")
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
            HStack {
                // sized to match the dots above
                Text("💰")
                    .font(.system(size: 12))
                    .frame(width: 12, height: 12)
                Text("Premium location, costs higher")
            }

            Text("**Pricing Calculator**").padding(.top)
            Divider()
            Text("[Estimate cost](https://upvpn.app/pricing-calculator/) of standard locations on pay as you go, or premium locations on any plan")

            Text("**Questions about product or pricing?**").padding(.top)
            Divider()
            Text(
                """
                Visit [FAQ](https://upvpn.app/faq/) page

                Or email us at [support@upvpn.app](mailto:support@upvpn.app) and we'll be happy to assist!
                """
            )

            Spacer()
            Text(
                """
                To delete your account, visit the [account page on the dashboard](https://upvpn.app/dashboard/account)


                [Acknowledgements](https://upvpn.app/oss/apple/)
                """
            ).font(.caption).foregroundStyle(.secondary)
        }
        .multilineTextAlignment(.leading)
        .textSelection(.enabled)
        .padding()
        .padding()
        .navigationTitle("Help")
        .frame(maxWidth: .infinity)
        // every web link on this page (FAQ, pricing calculator, dashboard, acknowledgements)
        // opens inside the app; anything else (mailto) goes to the system
        .environment(\.openURL, OpenURLAction { url in
            if let scheme = url.scheme?.lowercased(), scheme == "https" || scheme == "http" {
                presentedPage = WebPage(url: url)
                return .handled
            }
            return .systemAction
        })
        .sheet(item: $presentedPage) { page in
            InAppBrowserView(url: page.url)
        }
    }
}

#if os(iOS)
/// A web page in an in-app Safari view: the platform's own way to show a web page without
/// leaving the app, with its Done button and share sheet.
struct InAppBrowserView: UIViewControllerRepresentable {
    var url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let configuration = SFSafariViewController.Configuration()
        configuration.entersReaderIfAvailable = false
        return SFSafariViewController(url: url, configuration: configuration)
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {}
}
#elseif os(macOS)
/// A web page in a web view inside a sheet, so that the user does not have to leave the
/// app, with a way to open it in the browser instead.
struct InAppBrowserView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL

    var url: URL

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text(url.host ?? "")
                    .font(.headline)
                Spacer()
                Button("Open in Browser") {
                    openURL(url)
                }
                Button("Done") {
                    dismiss()
                }
                .keyboardShortcut(.defaultAction)
            }
            .padding()

            Divider()

            WebView(url: url)
        }
        .frame(minWidth: 800, minHeight: 650)
    }
}

struct WebView: NSViewRepresentable {
    var url: URL

    func makeNSView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateNSView(_ nsView: WKWebView, context: Context) {}
}
#endif

#Preview {
    HelpView()
}
