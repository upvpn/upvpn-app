## UpVPN on Apple Platform

The native macOS, tvOS, iOS, and iPadOS apps are built using the following modern Apple technologies: Swift, Swift Concurrency, SwiftUI, and Network Extension.

[Download it from the App Store](https://apps.apple.com/app/serverless-vpn-upvpn/id6596774170)


## Building

UpVPN is an Xcode project. Before buliding in Xcode, create a `UpVPN/Config/Developer.xcconfig` file with the following contents:

Note: For Network Extension development and running it on physical hardware, an Apple Developer Program membership is required. More information about this can be found here:
https://developer.apple.com/help/account/reference/supported-capabilities-ios


```
// You Apple developer account's Team ID.
DEVELOPMENT_TEAM =  YOUR_TEAM

// The bundle identifier of the apps.
// You can also use same bundle ID for all apps.
APP_ID_IOS = your-reverse-domain.ios
APP_ID_MACOS = your-reverse-domain.macos
APP_ID_TVOS = your-reverse-domain.tvos

// Absolute Path to the directory that Xcode creates in:
//  ~/Library/Developer/Xcode/DerivedData/UpVPN-<random>/SourcePackages
// (This is the path to the local git checkout of the Swift package dependency for WireGuardKit.)
SOURCE_PACKAGES_DIR = <absolute-path-to-sources-packages>
```


Note about `SOURCE_PACKAGES_DIR`: It's configured here so that WireGuardBridge<platform> can be built for `#Preview` just like Debug and Release. 

Ideally, for `#Preview`, we wouldn't want to build the WireGuardBridge dependency target because Network Extension is not going to run on the Simulator anyway. However, there's no easyway to disable that. Since `#Preview` makes UI development with SwiftUI easier, providing an absolute path for the WireGuardKit Swift package manually keeps build tool happy for all use cases.

Because `#Preview` builds for simulator environment, we also configure `"GOOS_iphonesimulator[sdk=iphonesimulator*]" = ios` in the Build Settings of WireGuardBridge.

## Dependency

All apps depend on the upstream wireguard-apple project. However, to make it work with recent versions of Xcode, including Xcode 16, changes have been made in the forked repository [upvpn/wireguard-apple](https://github.com/upvpn/wireguard-apple). This forked version is used as an Xcode depedency for the UpVPN project.

## Product Pages

Learn more at:
- [macOS](https://upvpn.app/macos)
- [iOS | iPadOS](https://upvpn.app/ios)
- [tvOS](https://upvpn.app/tvos)

