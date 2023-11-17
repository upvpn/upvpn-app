package app.upvpn.upvpn.data

import android.util.Log
import app.upvpn.upvpn.data.db.VPNDatabase
import app.upvpn.upvpn.model.VpnSessionStatus
import app.upvpn.upvpn.model.VpnSessionStatusRequest
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.delay

class VPNSessionWatcher(
    private val request: VpnSessionStatusRequest,
    private val vpnDatabase: VPNDatabase,
    private val vpnSessionRepository: VPNSessionRepository
) {
    private val tag = "VPNSessionWatcher"
    suspend fun watch(block: (VpnSessionStatus) -> Unit) {
        var done = false
        while (done.not()) {
            Log.i(tag, "watching ...")
            delay(1000)
            val status = vpnSessionRepository.getVpnSessionStatus(request)

            status.onSuccess(block)
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

            status.onFailure { Log.i(tag, "Failed to get status in watcher $it") }

        }
        Log.i(tag, "watch ended")
    }
}
