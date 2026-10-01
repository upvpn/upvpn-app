package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.R
import app.upvpn.upvpn.service.VPNState

// a session which is not yet connected can be ended by user only after
// this much time since it was requested
const val END_SESSION_THRESHOLD_MS = 10_000L

fun VPNState.toVPNUiState(): VpnUiState {
    return when (this) {
        is VPNState.Disconnected -> VpnUiState.Disconnected
        is VPNState.Requesting -> VpnUiState.Requesting(this.location)
        is VPNState.Accepted -> VpnUiState.Accepted(this.location, this.requestedAt)
        is VPNState.ServerCreated -> VpnUiState.ServerCreated(this.location, this.requestedAt)
        is VPNState.ServerRunning -> VpnUiState.ServerRunning(this.location, this.requestedAt)
        is VPNState.ServerReady -> VpnUiState.ServerReady(this.location)
        is VPNState.Connecting -> VpnUiState.Connecting(this.location)
        is VPNState.Connected -> VpnUiState.Connected(this.location, this.time)
        is VPNState.Disconnecting -> VpnUiState.Disconnecting(this.location)
    }
}

fun VpnUiState.shieldResourceId(): Int {
    return when (this) {
        is VpnUiState.Connected -> R.drawable.vpn_on
        else -> R.drawable.vpn_off
    }
}

fun VpnUiState.vpnDisplayText(): String {
    return when (this) {
        is VpnUiState.Requesting -> "Requesting"
        is VpnUiState.Accepted -> "Accepted"
        is VpnUiState.ServerCreated -> "Server Created"
        is VpnUiState.ServerRunning -> "Server Running"
        is VpnUiState.ServerReady -> "Server Ready"
        is VpnUiState.Connecting -> "Connecting"
        is VpnUiState.Connected -> "VPN is on"
        is VpnUiState.Disconnecting -> "Disconnecting"
        else -> "VPN is off"
    }
}

fun VpnUiState.isVpnSessionActivityInProgress(): Boolean = (this is VpnUiState.Disconnected).not()

fun VpnUiState.isConnectedOrDisconnectedOrDisconnecting(): Boolean = (this is VpnUiState.Connected
        || this is VpnUiState.Disconnected
        || this is VpnUiState.Disconnecting)


// time (ms) left until session can be ended by user: 0 when it can be ended now,
// null when it cannot be ended in this state no matter the time.
// now is SystemClock.elapsedRealtime()
fun VpnUiState.endSessionRemainingMs(
    now: Long,
    thresholdMs: Long = END_SESSION_THRESHOLD_MS
): Long? {
    val requestedAt = when (this) {
        is VpnUiState.Connected -> return 0L
        is VpnUiState.Accepted -> this.requestedAt
        is VpnUiState.ServerCreated -> this.requestedAt
        is VpnUiState.ServerRunning -> this.requestedAt
        is VpnUiState.Checking,
        is VpnUiState.Disconnected,
        is VpnUiState.Requesting,
        is VpnUiState.ServerReady,
        is VpnUiState.Connecting,
        is VpnUiState.Disconnecting -> return null
    }

    return (thresholdMs - (now - requestedAt)).coerceAtLeast(0L)
}

fun VpnUiState.canEndSession(now: Long, thresholdMs: Long = END_SESSION_THRESHOLD_MS): Boolean =
    this.endSessionRemainingMs(now, thresholdMs) == 0L

fun VpnUiState.switchEnabled(now: Long, thresholdMs: Long = END_SESSION_THRESHOLD_MS): Boolean =
    this is VpnUiState.Disconnected || this.canEndSession(now, thresholdMs)

fun VpnUiState.switchChecked(): Boolean {
    return when (this) {
        is VpnUiState.Checking -> false
        is VpnUiState.Requesting -> true
        is VpnUiState.Accepted -> true
        is VpnUiState.ServerCreated -> true
        is VpnUiState.ServerRunning -> true
        is VpnUiState.ServerReady -> true
        is VpnUiState.Connecting -> true
        is VpnUiState.Connected -> true
        is VpnUiState.Disconnecting -> false
        is VpnUiState.Disconnected -> false
    }
}

fun VpnUiState.progress(): Float {
    return when (this) {
        is VpnUiState.Checking,
        is VpnUiState.Requesting -> 0.10f

        is VpnUiState.Accepted -> 0.25f
        is VpnUiState.ServerCreated -> 0.5f
        is VpnUiState.ServerRunning -> 0.75f
        is VpnUiState.ServerReady -> 0.8f
        is VpnUiState.Connecting -> 0.95f
        is VpnUiState.Connected -> 1f
        is VpnUiState.Disconnecting,
        is VpnUiState.Disconnected -> 0f
    }
}

fun VpnUiState.transitionToDisconnecting(): VpnUiState? {
    return when (this) {
        is VpnUiState.Disconnected, is VpnUiState.Checking -> null
        is VpnUiState.Requesting -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.Accepted -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.ServerCreated -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.ServerRunning -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.ServerReady -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.Connecting -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.Connected -> VpnUiState.Disconnecting(this.location)
        is VpnUiState.Disconnecting -> this
    }
}
