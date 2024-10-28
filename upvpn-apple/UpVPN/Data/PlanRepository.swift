//
//  PlanRepository.swift
//  UpVPN
//
//  Created by Himanshu on 8/6/24.
//

import Foundation

struct PlanError: Error {
    var message: String
}

extension PlanError: CustomStringConvertible {
    var description: String {
        return self.message
    }
}

class PlanRepository {
    static var shared = PlanRepository(planApiService: DefaultVpnApiService.shared)

    private var planApiService: PlanApiService

    private init(planApiService: PlanApiService) {
        self.planApiService = planApiService
    }

    func getUserPlan() async -> Result<UserPlan, PlanError> {
        return await self.planApiService.getUserPlan()
            .mapError { apiError in PlanError(message: apiError.message)}
    }

    func processTransaction(environment: String, id: UInt64) async -> Result<(), PlanError> {
        return await self.planApiService.processTransaction(environment: environment, id: id)
            .mapError { apiError in PlanError(message: apiError.message)}
    }
}
