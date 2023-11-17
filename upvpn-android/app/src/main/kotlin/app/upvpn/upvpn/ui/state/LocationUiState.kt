package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.model.Location

data class LocationUiState(
    val locationState: LocationState = LocationState.Loading,
    val search: String = "",
    val selectedLocation: Location? = null
)

sealed class LocationState {
    data object Loading : LocationState()

    data class Locations(val locations: List<Location>) : LocationState()

    data class Error(val message: String) : LocationState()
}
