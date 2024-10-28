//
//  VpnSessionRepository.swift
//  UpVPN
//
//  Created by Himanshu on 7/23/24.
//

import Foundation
import WireGuardKit

enum VPNSessionRepositoryError : Error {
    case store(StoreError)
    case api(ApiError)
    case other(String)
}

extension VPNSessionRepositoryError : CustomStringConvertible {
    var description: String {
        return switch self {
        case .store(let storeError):
            storeError.description
        case .api(let apiError):
            apiError.description
        case .other(let string):
            string
        }
    }
}

protocol VPNSessionRepository {
    func newVpnSession(requestId: UUID,
                       location: Location,
                       onStatusUpdate: @escaping (VpnSessionStatus, Location) -> Void)
        async -> Result<(Accepted, InterfaceConfiguration),  VPNSessionRepositoryError>

    func getVpnSessionStatus(request: VpnSessionStatusRequest) async -> Result<VpnSessionStatus, VPNSessionRepositoryError>
    func endVpnSession(meta: SessionMeta, reason: String) async -> Result<Ended, VPNSessionRepositoryError>
}

class DefaultVPNSessionRepository : VPNSessionRepository {
    private var vpnApiService : VPNApiService = DefaultVpnApiService.shared
    private var vpnSessionStore = VPNSessionStore.shared
    private var vpnSessionWatcherTask : Task<Void, Never>? = nil

    init() {
        // on first load to memory mark previous sessions for deletion
         self.vpnSessionStore.markAllForDeletion()
    }

    private func startVpnSessionWatcherTask(request: VpnSessionStatusRequest,
                                            location: Location,
                                            onStatusUpdate: @escaping (VpnSessionStatus, Location) -> Void) {
        if self.vpnSessionWatcherTask == nil {
            self.vpnSessionWatcherTask = VPNSessionWatcher(request: request,
                                                           vpnSessionRepository: self,
                                                           location: location,
                                                           onStatusUpdate: onStatusUpdate).watch()
        }
    }

    private func stopVpnSessionWatcherTask() {
        self.vpnSessionWatcherTask?.cancel()
        self.vpnSessionWatcherTask = nil
    }

    func newVpnSession(requestId: UUID,
                       location: Location,
                       onStatusUpdate: @escaping (VpnSessionStatus, Location) -> Void) async -> Result<(Accepted, InterfaceConfiguration), VPNSessionRepositoryError> {
        // wrapped in do catch to avoid nested switch statements for enum cases
        do {
            let device = try await DeviceStore.getDevice().get()

            guard let device = device else {
                return .failure(.other("no device found"))
            }

            // reclaim any past sessions
            await VPNSessionReclaimer().run(deviceUniqueId: device.uniqueId, vpnApiService: self.vpnApiService)

            let newSession = NewSession(requestId: requestId,
                                        deviceUniqueId: device.uniqueId,
                                        locationCode: location.code)
            // make api call
            let accepted = try await self.vpnApiService.newVpnSession(request: newSession).get()

            var interfaceConfiguration = InterfaceConfiguration(from: device)
            interfaceConfiguration.dns = [DNSServer(from: "1.1.1.1")!]
            interfaceConfiguration.mtu = 1280

            // store session locally (ideally this should be saved before making api call)
            await self.vpnSessionStore.save(requestId: requestId)

            // start watcher
            let request = VpnSessionStatusRequest(requestId: newSession.requestId, 
                                                  deviceUniqueId: newSession.deviceUniqueId,
                                                  vpnSessionUuid: accepted.vpnSessionUuid)

            self.startVpnSessionWatcherTask(request: request,
                                            location: location,
                                            onStatusUpdate: onStatusUpdate)

            return .success((accepted, interfaceConfiguration))
        } catch let error as StoreError {
            return .failure(.store(error))
        } catch let error as ApiError {
            return .failure(.api(error))
        } catch {
            // not expected but need to make compiler happy for calling .get() on Result
            return .failure(.other("\(error.localizedDescription)"))
        }
    }
    
    func getVpnSessionStatus(request: VpnSessionStatusRequest) async -> Result<VpnSessionStatus, VPNSessionRepositoryError> {
        return await self.vpnApiService.getVpnSessionStatus(request: request)
            .mapError(VPNSessionRepositoryError.api)
    }
    
    func endVpnSession(meta: SessionMeta, reason: String) async -> Result<Ended, VPNSessionRepositoryError> {
        // stop watcher
        self.stopVpnSessionWatcherTask()

        do {
            // make api call
            let device = try await DeviceStore.getDevice().get()

            guard let device = device else {
                return .failure(.other("device not found for ending vpn session"))
            }

            let request = EndSessionApi(requestId: meta.requestId,
                                        deviceUniqueId: device.uniqueId,
                                        vpnSessionUuid: meta.vpnSessionUuid,
                                        reason: reason)
            let result = await self.vpnApiService.endVpnSession(request: request).mapError(VPNSessionRepositoryError.api)

            if case .success = result {
                // delete local session
                await self.vpnSessionStore.delete(requestId: meta.requestId)
            } else {
                // mark session for deletion
                await self.vpnSessionStore.markForDeletion(requestId: meta.requestId)
            }

            return result
        } catch let error as StoreError {
            return .failure(.store(error))
        } catch let error as ApiError {
            return .failure(.api(error))
        } catch {
            // not expected but need to make compiler happy for calling .get() on Result
            return .failure(.other("\(error.localizedDescription)"))
        }

    }
}
