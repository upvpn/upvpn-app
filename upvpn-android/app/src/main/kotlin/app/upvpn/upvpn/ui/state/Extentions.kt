package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.R
import app.upvpn.upvpn.service.VPNState

fun VPNState.toVPNUiState(): VpnUiState {
    return when (this) {
        is VPNState.Disconnected -> VpnUiState.Disconnected
        is VPNState.Requesting -> VpnUiState.Requesting(this.location)
        is VPNState.Accepted -> VpnUiState.Accepted(this.location)
        is VPNState.ServerCreated -> VpnUiState.ServerCreated(this.location)
        is VPNState.ServerRunning -> VpnUiState.ServerRunning(this.location)
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


fun VpnUiState.switchEnabled(): Boolean {
    return when (this) {
        is VpnUiState.Connected,
        is VpnUiState.Disconnected -> true

        else -> false
    }
}

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
