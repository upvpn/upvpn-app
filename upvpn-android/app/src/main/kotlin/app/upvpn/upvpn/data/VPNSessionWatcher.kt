package app.upvpn.upvpn.data

import android.util.Log
import app.upvpn.upvpn.data.db.VPNDatabase
import app.upvpn.upvpn.model.Failed
import app.upvpn.upvpn.model.VpnSessionStatus
import app.upvpn.upvpn.model.VpnSessionStatusRequest
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.delay

class VPNSessionWatcher(
    private val request: VpnSessionStatusRequest,
    private val vpnDatabase: VPNDatabase,
    private val vpnSessionRepository: VPNSessionRepository
) {
    private val tag = "VPNSessionWatcher"
    suspend fun watch(block: (status: VpnSessionStatus, isUnauthorized: Boolean) -> Unit) {
        var done = false
        while (done.not()) {
            Log.i(tag, "watching ...")
            delay(1000)
            val result = vpnSessionRepository.getVpnSessionStatus(request)

            // when unauthorized session cannot be watched anymore, report it as failed
            val isUnauthorized = result.getError() == "unauthorized"
            val status: Result<VpnSessionStatus, String> = if (isUnauthorized) {
                Ok(VpnSessionStatus.Failed(Failed(request.requestId, request.vpnSessionUuid)))
            } else {
                result
            }

            status.onSuccess { block(it, isUnauthorized) }
            // if the status is end state break
            status.onSuccess {
                if (it is VpnSessionStatus.ServerReady || it is VpnSessionStatus.Failed || it is VpnSessionStatus.Ended) {
                    done = true
                }
            }

            status.onSuccess {
                if (it is VpnSessionStatus.Failed || it is VpnSessionStatus.Ended) {
                    vpnDatabase.vpnSessionDao().deleteWithRequestId(request.requestId)
                }
            }

            result.onFailure { Log.i(tag, "Failed to get status in watcher $it") }

        }
        Log.i(tag, "watch ended")
    }
}
