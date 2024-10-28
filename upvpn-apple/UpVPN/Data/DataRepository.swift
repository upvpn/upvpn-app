//
//  DataRepository.swift
//  UpVPN
//
//  Created by Himanshu on 7/18/24.
//

import Foundation

struct DataRepoError: Error {
    var message: String
}

class DataRepository {

    static var shared = DataRepository(vpnApiService: DefaultVpnApiService.shared)

    private var vpnApiService: VPNApiService
    private var locationStore: LocationStore = LocationStore()

    private init(vpnApiService: VPNApiService) {
        self.vpnApiService = vpnApiService
    }

    func initializeDevice() async -> Result<(), DataRepoError> {

        let result = await DeviceStore.getOrInitializeDevice()

        return switch result {
        case .success(_):
                .success(())
        case .failure(let storeError):
                .failure(DataRepoError(message: "failed to initialize device: \(storeError)"))
        }
    }

    func isAuthenticated() async -> (String?, Device?) {
        if case .success(let device) = await DeviceStore.getDevice(), let device = device {
            if case .some = device.token {
                if case .success(let userCredentials) = await UserStore.getCredentials() {
                    return (userCredentials?.email, device)
                }
            }
        }
        return (nil, nil)
    }

    func addDevice(userCredentials: UserCredentials) async -> Result<Device, DataRepoError> {
        do {
            var device = try await DeviceStore.getOrInitializeDevice().get()
            // make api call
            let deviceInfo = DeviceInfo(from: device)
            let addDeviceResponse = try await self.vpnApiService.addDevice(request: AddDeviceRequest(userCreds: userCredentials,
                                          deviceInfo: deviceInfo)).get()
            // update device in DeviceStore
            // add user credentials to UserStore
            device.token = addDeviceResponse.token
            device.ipv4Address = addDeviceResponse.deviceAddresses.ipv4Address
            
            // todo: should handle error?
            let _ = await UserStore.saveCredentials(userCredentials: userCredentials)

            try await DeviceStore.updateDevice(device: device).get()

            return .success(device)
        } catch {
            return .failure(DataRepoError(message: "\(error)"))
        }
    }

    func signOut() async -> Result<(), DataRepoError> {
        do {
            // make api call
            if case .failure(let error) = await self.vpnApiService.signOut() {
                // if the token is already "unauthorized" we can continue
                if !error.isUnauthorized() {
                    throw error
                }
            }
            // update local storage
            try await DeviceStore.deleteDevice().get()
            return .success(())
        } catch {
            print("sign out error: \(error)")
            return .failure(DataRepoError(message: "\(error)"))
        }
    }

    func requestCode(email: String) async -> Result<(), DataRepoError> {
        await self.vpnApiService.requestCode(onlyEmail: OnlyEmail(email: email))
            .mapError { e in
                DataRepoError(message: e.message)
            }
    }

    func signUp(userCredsWithCode: UserCredentialsWithCode) async -> Result<(), DataRepoError> {
        await self.vpnApiService.signUp(userCredsWithCode: userCredsWithCode)
            .mapError { e in
                DataRepoError(message: e.message)
            }
    }

    func getLocations() async -> AsyncStream<[Location]> {
        let (stream, continuation) = AsyncStream.makeStream(of: [Location].self,
                                                            bufferingPolicy: .unbounded)
        Task {
            if let locations = try? await LocationStore.load() {
                continuation.yield(locations)
            }

            if case .success(let locations) = await self.vpnApiService.getLocations() {
                continuation.yield(locations)
                try? await LocationStore.save(locations: locations)
            }

            continuation.finish()

        }
        return stream
    }

    func addRecent(location: Location) async {
        await self.locationStore.saveRecent(code: location.code)
    }

    func loadRecent() async -> [Location] {
        var recentLocations: [Location] = []
        if let locations = try? await LocationStore.load() {
            let recents = await self.locationStore.loadRecent()
            // preserve the order of recents
            for recent in recents {
                if let found = locations.first(where: { recent == $0.code }) {
                    recentLocations.append(found)
                }
            }
        }
        return recentLocations
    }
}
