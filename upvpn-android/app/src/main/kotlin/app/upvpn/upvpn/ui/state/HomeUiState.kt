package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.model.Location

data class HomeUiState(
    val vpnUiState: VpnUiState = VpnUiState.Checking
)

sealed class VpnUiState {
    data object Checking : VpnUiState()
    data object Disconnected : VpnUiState()
    data class Requesting(val location: Location) : VpnUiState()
    data class Accepted(val location: Location) : VpnUiState()
    data class ServerCreated(val location: Location) : VpnUiState()
    data class ServerRunning(val location: Location) : VpnUiState()
    data class ServerReady(val location: Location) : VpnUiState()
    data class Connecting(val location: Location) : VpnUiState()
    data class Connected(val location: Location, val time: Long) : VpnUiState()
    data class Disconnecting(val location: Location) : VpnUiState()
}
