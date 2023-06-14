<div align="center">
    <a href="https://upvpn.app">
        <img src="./upvpn-assets/icons/Square71x71Logo.png" >
    </a>
    <h3 align="center">upvpn</h3>
    <h4 align="center">A Modern Serverless VPN</h4>
</div>

# upvpn

upvpn (pronounced Up VPN) lets you connect to the internet from a location of your choice.
For more information please visit https://upvpn.app

upvpn app is made up of UI, CLI and background Daemon.

# Serverless

upvpn uses Serverless computing model, where a Linux based WireGuard server is provisioned on public cloud providers when app requests to connect to VPN. And server is deprovisioned when app requests to disconnect from VPN.

All of it happens with a single click on the UI, or a single command on terminal.

# Install
Installer for Linux, macOS, and Windows is available for download on [Github Releases](https://github.com/upvpn/upvpn-app/releases) or on website at https://upvpn.app/download


# Code

## Organization

| Crate or Directory | Description |
| --- | --- |
| upvpn-cli | Code for `upvpn` cli |
| upvpn-config | Configuration read from env vars, `upvpn.conf.toml` are merged at runtime in `upvpn-config` and is source of runtime configuration for `upvpn-cli`, `upvpn-daemon`, and `upvpn-ui`. |
| upvpn-controller | Defines GRPC protobuf for APIs exposed by `upvpn-daemon` to be consumed by `upvpn-cli` and `upvpn-ui`. |
| upvpn-daemon | Daemon is responsible for orchestrating a VPN session. It takes input from upvpn-cli or upvpn-ui via GRPC (defined in `upvpn-controller`) and make calls to backend server via separate GRPC (defined in `upvpn-server`). When backend informs that a server is ready daemon configures network tunnel, see [NetworkDependency.md](./NetworkDependency.md) for more info. |
| upvpn-entity | Defines data models used by upvpn-daemon to persist data on disk in sqlite database. |
| upvpn-migration | Defines database migration from which `upvpn-entity` is generated |
| upvpn-packages| Contains resources to package binaries for distribution on macOS (pkg), Linux (rpm & deb), and Windows (msi) |
|upvpn-server| Contains GRPC protobuf definitions and code for communication with backend server |
| upvpn-types | Defines common Rust types for data types used in various crates. These are also used to generate Typescript types for upvpn-ui for seamless serialization and deserialization across language boundaries |
|upvpn-ui| A Tauri based desktop app. GPRC communication with daemon is done in Rust. Typescript code interact with Rust code via Tauri commands |



## Network Dependency

For networking code, please see [NetworkDependency.md](./NetworkDependency.md)

## Building

Please see [Build.md](./Build.md)

# License

All Rust crates in this repository are [licensed under GPL version 3](./LICENSE).

Copyright (C) 2023  upvpn LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
