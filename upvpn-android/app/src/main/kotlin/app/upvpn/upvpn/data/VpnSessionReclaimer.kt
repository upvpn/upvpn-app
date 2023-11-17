package app.upvpn.upvpn.data

import android.util.Log
import app.upvpn.upvpn.data.db.VPNDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VpnSessionReclaimer(
    private val vpnDatabase: VPNDatabase,
    private val vpnSessionRepository: VPNSessionRepository
) {
    private val tag = "VPNSessionReclaimer"

    private var _running = true

    fun running(): Boolean {
        return _running
    }

    fun reclaim(scope: CoroutineScope) {
        Log.i(tag, "Starting Reclaimer")
        scope.launch {
            while (true) {
                // run until there are no more session to be reclaimed
                val requestIds = vpnDatabase.vpnSessionDao().toReclaim()

                if (requestIds.isEmpty()) {
                    Log.i(tag, "No more sessions to reclaim")
                    break
                }

                for (requestId in requestIds) {
                    Log.i(tag, "Reclaiming $requestId")
                    vpnSessionRepository.endVpnSession(scope, requestId, "reclaimed")
                }

                delay(10000)
            }

            Log.i(tag, "Stopping Reclaimer")
            _running = false
        }
    }
}
