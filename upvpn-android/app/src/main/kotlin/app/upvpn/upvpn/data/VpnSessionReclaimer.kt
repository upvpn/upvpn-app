package app.upvpn.upvpn.data

import android.util.Log
import app.upvpn.upvpn.data.db.VPNDatabase
import kotlinx.coroutines.CoroutineScope

class VpnSessionReclaimer(
    private val vpnDatabase: VPNDatabase,
    private val vpnSessionRepository: VPNSessionRepository
) {
    private val tag = "VPNSessionReclaimer"

    suspend fun reclaim(scope: CoroutineScope) {
        Log.i(tag, "Reclaiming")
        // requests to be reclaimed
        val requestIds = vpnDatabase.vpnSessionDao().toReclaim()

        if (requestIds.isEmpty()) {
            Log.i(tag, "No more sessions to reclaim")
        } else {
            for (requestId in requestIds) {
                Log.i(tag, "Reclaiming $requestId")
                val job = vpnSessionRepository.endVpnSession(scope, requestId, "reclaimed")
                job.join()
            }
        }
    }
}
