//
//  VPNApiService.swift
//  UpVPN
//
//  Created by Himanshu on 7/10/24.
//

import Foundation

protocol VPNApiService {

    func addDevice(request: AddDeviceRequest) async -> Result<AddDeviceResponse, ApiError>

    func signOut() async -> Result<(), ApiError>

    func getLocations() async -> Result<[Location], ApiError>

    func newVpnSession(request: NewSession) async -> Result<Accepted, ApiError>

    func getVpnSessionStatus(request: VpnSessionStatusRequest) async -> Result<VpnSessionStatus, ApiError>

    func endVpnSession(request: EndSessionApi) async -> Result<Ended, ApiError>

    func requestCode(onlyEmail: OnlyEmail) async -> Result<(), ApiError>

    func signUp(userCredsWithCode: UserCredentialsWithCode) async -> Result<(), ApiError>

}

protocol PlanApiService {
    func getUserPlan() async -> Result<UserPlan, ApiError>
    func processTransaction(environment: String, id: UInt64) async -> Result<(), ApiError>
}

class DefaultVpnApiService: VPNApiService, PlanApiService {

    static var shared = DefaultVpnApiService()

    private var getAuthToken: () async -> String?

    private init() {
        getAuthToken = {
            guard case .success(let device) = await DeviceStore.getDevice() else {
                return nil
            }
            return device?.token
        }
    }

    private lazy var client: Client  = {
        return Client(baseURL: AppConfig.baseURL, getAuthToken: self.getAuthToken)
    }()

    func addDevice(request: AddDeviceRequest) async -> Result<AddDeviceResponse, ApiError> {
        return await self.client.request("devices", method: .post, body: encodeToData(request))
            .mapError(mapClientError)
    }
    
    func signOut() async -> Result<(), ApiError> {
        let result: Result<Empty, ApiError> = await self.client.request("sign-out", method: .post)
            .mapError(mapClientError)

        return result.map { _ in () }
    }

    func signUp(userCredsWithCode: UserCredentialsWithCode) async -> Result<(), ApiError> {
        let result: Result<Empty, ApiError> = await self.client.request("account", method: .post,
                                                                        body: encodeToData(userCredsWithCode))
            .mapError(mapClientError)
        return result.map { _ in () }
    }

    func requestCode(onlyEmail: OnlyEmail) async -> Result<(), ApiError> {
        let result: Result<Empty, ApiError> = await self.client.request("account/send-code", method: .post,
                                                                        body: encodeToData(onlyEmail))
            .mapError(mapClientError)
        return result.map { _ in () }
    }

    func getLocations() async -> Result<[Location], ApiError> {
        return await self.client.request("locations")
            .mapError(mapClientError)
    }
    
    func newVpnSession(request: NewSession) async -> Result<Accepted, ApiError> {
        return await self.client.request("new-vpn-session", method: .post, body: encodeToData(request))
            .mapError(mapClientError)
    }
    
    func getVpnSessionStatus(request: VpnSessionStatusRequest) async -> Result<VpnSessionStatus, ApiError> {
        return await self.client.request("vpn-session-status", method: .post, body: encodeToData(request))
            .mapError(mapClientError)
    }
    
    func endVpnSession(request: EndSessionApi) async -> Result<Ended, ApiError> {
        return await self.client.request("end-vpn-session", method: .post, body: encodeToData(request))
            .mapError(mapClientError)
    }

    func processTransaction(environment: String, id: UInt64) async -> Result<(), ApiError> {
        let result: Result<Empty, ApiError> = await self.client.request("iap/transaction/\(environment)/\(id)", method: .post)
            .mapError(mapClientError)

        return result.map { _ in () }
    }

    func getUserPlan() async -> Result<UserPlan, ApiError> {
        return await self.client.request("plan/current")
            .mapError(mapClientError)
    }
}
