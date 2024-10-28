package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.model.Location

data class LocationUiState(
    val locations: List<Location> = listOf<Location>(),
    val search: String = "",
    val selectedLocation: Location? = null,
    val isLoading: Boolean = true,
    val locationFetchError: String? = null,
)
