package app.upvpn.upvpn.ui.state

import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.LocationTier

data class LocationUiState(
    val locations: List<Location> = listOf<Location>(),
    val search: String = "",
    // null shows every tier
    val tierFilter: LocationTier? = null,
    val selectedLocation: Location? = null,
    val isLoading: Boolean = true,
    val locationFetchError: String? = null,
)
