package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.model.Location

data class HomeUiState(
    val vpnUiState: VpnUiState = VpnUiState.Checking
)

sealed class VpnUiState {
    data object Checking : VpnUiState()
    data object Disconnected : VpnUiState()
    data class Requesting(val location: Location) : VpnUiState()
    // requestedAt is SystemClock.elapsedRealtime() of when new vpn session was accepted
    data class Accepted(val location: Location, val requestedAt: Long) : VpnUiState()
    data class ServerCreated(val location: Location, val requestedAt: Long) : VpnUiState()
    data class ServerRunning(val location: Location, val requestedAt: Long) : VpnUiState()
    data class ServerReady(val location: Location) : VpnUiState()
    data class Connecting(val location: Location) : VpnUiState()
    data class Connected(val location: Location, val time: Long) : VpnUiState()
    data class Disconnecting(val location: Location) : VpnUiState()
}

fun VpnUiState.getLocation(): Location? {
    return when (this) {
        is VpnUiState.Checking, is VpnUiState.Disconnected -> null
        is VpnUiState.Requesting -> location
        is VpnUiState.Accepted -> location
        is VpnUiState.ServerCreated -> location
        is VpnUiState.ServerRunning -> location
        is VpnUiState.ServerReady -> location
        is VpnUiState.Connecting -> location
        is VpnUiState.Connected -> location
        is VpnUiState.Disconnecting -> location
    }
}
