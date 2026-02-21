//
//  ResponsiveView.swift
//  UpVPNiOS
//
//  Created by Himanshu on 9/3/24.
//

import SwiftUI
import CoreGraphics

extension UIApplication {
    public static var isSplitOrSlideOver: Bool {
        guard let screen = UIApplication.shared.connectedScenes.first as? UIWindowScene else {
            return false
        }
        return screen.windows.first?.frame.size != screen.screen.bounds.size
    }
}

enum LayoutShape {
    case phone
    case padPortrait
    case padLandscape
}

private func getLayoutShape(size: CGSize) -> LayoutShape {
    var shape = LayoutShape.phone
    let isPad = UIDevice.current.userInterfaceIdiom == .pad
    let isLandscape = size.width > size.height

    // when split or sideover is wide enough to show landscape
    if UIApplication.isSplitOrSlideOver && isPad && isLandscape && size.width > 600 {
        shape = LayoutShape.padLandscape
    }

    // full screen on iPad
    if !UIApplication.isSplitOrSlideOver && isPad {
        if size.width > size.height {
            shape = .padLandscape
        } else {
            shape = .padPortrait
        }
    }

    return shape
}

enum MapOrConfig {
    case map
    case config
}

struct ResponsiveHomeView: View {

    @EnvironmentObject private var tunnelViewModel: TunnelViewModel
    @EnvironmentObject private var authViewModel: AuthViewModel
    @EnvironmentObject private var locationViewModel: LocationViewModel

    @State private var showMapOrConfig = MapOrConfig.map

    var onStatsTap: () -> Void = {}

    var body: some View {
        GeometryReader { reader in

            let layoutShape = getLayoutShape(size: reader.size)

            switch layoutShape {
            case .phone:
                HomeView(onStatsTap: onStatsTap)
            case .padLandscape:
                HStack(spacing: 0) {
                    HomeView(onStatsTap: { showMapOrConfig = MapOrConfig.config })
                        .frame(maxWidth: 500)

                    RuntimeConfigMapView(showMapOrConfig: $showMapOrConfig,
                                         mapModifier: LandscapeMapModifier(),
                                         configModifier: LandscapeRuntimeConfigModifier(),
                                         pickerModifier: LandscapePickerModifier())
                        .background(Color.uSystemGroupedBackground)
                }
                .modifier(MapOrConfigOnTunnelStatusChange(
                    tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                    showMapOrConfig: $showMapOrConfig))
            case .padPortrait:
                VStack(spacing: 0) {
                    HStack(spacing: 0) {
                        HomeCard(tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                                 start: {
                            if let location = locationViewModel.selected {
                                locationViewModel.addRecent(location: location)
                                tunnelViewModel.start(to: location)
                            }
                        },
                                 stop: {
                            tunnelViewModel.stop()
                        })
                        .padding([.leading, .top, .bottom])
                        .frame(width: reader.size.width / 2)
                        .aspectRatio(1, contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .background(Color.uSystemGroupedBackground)

                        
                        if tunnelViewModel.tunnelObserver.tunnelStatus.isConnected(),
                            let runtimeConfig = tunnelViewModel.tunnelObserver.runtimeConfig,
                           let peer = runtimeConfig.peers.first,
                           let tx = peer.txBytes,
                           let rx  = peer.rxBytes

                        {
                            CardContainer {
                                StatsCard(tx: prettyBytes(tx), rx: prettyBytes(rx))
                            }
//                            .contentShape(Rectangle())
                            .frame(width: reader.size.width / 2)
                            .aspectRatio(1, contentMode: .fit)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .background(Color.uSystemGroupedBackground)
                            .onTapGesture {
                                showMapOrConfig = MapOrConfig.config
                            }
                        } else  {

                            if locationViewModel.recentLocations.isEmpty {
                                CardContainer {
                                    WelcomeView(showSpinnner: false)
                                }
                                .frame(width: reader.size.width / 2)
                                .aspectRatio(1, contentMode: .fit)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                .background(Color.uSystemGroupedBackground)
                            } else {
                                RecentLocationsCard()
                                    .frame(width: reader.size.width / 2)
                                    .aspectRatio(1, contentMode: .fit)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                                    .background(Color.uSystemGroupedBackground)
                            }
                        }

                    }

                    RuntimeConfigMapView(showMapOrConfig: $showMapOrConfig,
                                         mapModifier: PortraitMapModifier(),
                                         configModifier: PortraitRuntimeConfigurationModifier(),
                                         pickerModifier: PortraitPickerModifier())
                        .background(Color.uSystemGroupedBackground)
                }
                .modifier(MapOrConfigOnTunnelStatusChange(
                    tunnelStatus: tunnelViewModel.tunnelObserver.tunnelStatus,
                    showMapOrConfig: $showMapOrConfig))
            }
        }
        .frame(minWidth: 350)
    }
}


struct LandscapeMapModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .ignoresSafeArea(edges: .top)
            .padding(.top, 5)
            .padding([.trailing])
    }
}

struct LandscapeRuntimeConfigModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(.leading, -15)
            .padding(.top, -5)
    }
}

struct PortraitMapModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(.horizontal)
    }
}

struct PortraitRuntimeConfigurationModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
    }
}


struct LandscapePickerModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding([.top, .trailing, .bottom])
    }
}

struct PortraitPickerModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding()

    }
}

struct MapOrConfigOnTunnelStatusChange: ViewModifier {
    var tunnelStatus: TunnelStatus
    @Binding var showMapOrConfig: MapOrConfig

    func body(content: Content) -> some View {
        content
            .onChange(of: tunnelStatus) { newStatus in
                switch newStatus {
                case .connected, .disconnecting:
                    showMapOrConfig = .config
                default:
                    showMapOrConfig = .map
                }
            }
    }
}


#Preview {
    let locationViewModel = LocationViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true})

    return ResponsiveHomeView()
        .environmentObject(TunnelViewModel())
        .environmentObject(AuthViewModel(dataRepository: DataRepository.shared, isDisconnected: {return true}))
        .environmentObject(locationViewModel)
}
