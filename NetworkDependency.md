# Network Dependency

upvpn takes its design inspiration from [mullvadvpn-app](https://github.com/mullvad/mullvadvpn-app).
For example Actor pattern is foundational in daemon.

However, upvpn uses different design/technologies than mullvadvpn-app in many places: for persisting data in sqlite (sea-orm), desktop app (Tauri), packaging for Windows (Wix), Toml for configuration files, GRPC for backend API.

upvpn uses "talpid" crates for all of the client side networking from mullvadvpn-app project, with minimal modification. For example, interface on Linux would be named "upvpn".

The modifications to upstream mullvadvpn-app can be found [here](https://github.com/upvpn/mullvadvpn-app)

We're not affiliated with Mullvad, and we're grateful for their high quality open source projects. If upvpn doesn't offer what you're looking for please see VPN offering from Mullvad at https://mullvad.net/
