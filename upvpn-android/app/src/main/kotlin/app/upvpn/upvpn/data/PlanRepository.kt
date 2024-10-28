package app.upvpn.upvpn.data

import app.upvpn.upvpn.model.UserPlan
import app.upvpn.upvpn.network.VPNApiService
import app.upvpn.upvpn.network.toResult
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError

interface PlanRepository {
    suspend fun getUserPlan(): Result<UserPlan, String>
}

class DefaultPlanRepository(
    private val vpnApiService: VPNApiService,
) : PlanRepository {

    private val tag = "DefaultPlanRepository"

    override suspend fun getUserPlan(): Result<UserPlan, String> {
        return vpnApiService.getUserPlan().toResult()
            .mapError { e -> e.message }
    }
}
