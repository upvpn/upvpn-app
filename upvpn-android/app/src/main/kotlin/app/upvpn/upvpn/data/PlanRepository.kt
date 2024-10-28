package app.upvpn.upvpn.data

import app.upvpn.upvpn.data.db.VPNDatabase
import app.upvpn.upvpn.model.UserPlan
import app.upvpn.upvpn.network.VPNApiService
import app.upvpn.upvpn.network.toResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import java.util.UUID

interface PlanRepository {
    suspend fun getUserPlan(): Result<UserPlan, String>
    suspend fun getEmailAndDeviceId(): Result<EmailAndDeviceId, String>
}

data class EmailAndDeviceId(
    val email: String,
    val deviceId: UUID,
)

class DefaultPlanRepository(
    private val vpnApiService: VPNApiService,
    private val vpnDatabase: VPNDatabase,
) : PlanRepository {

    private val tag = "DefaultPlanRepository"

    override suspend fun getUserPlan(): Result<UserPlan, String> {
        return vpnApiService.getUserPlan().toResult()
            .mapError { e -> e.message }
    }

    override suspend fun getEmailAndDeviceId(): Result<EmailAndDeviceId, String> {
        val user = vpnDatabase.userDao().getUser()
        val device = vpnDatabase.deviceDao().getDevice()

        return if (user != null && device != null) {
            Ok(EmailAndDeviceId(user.email, device.uniqueId))
        } else {
            Err("user or device not found")
        }
    }
}
