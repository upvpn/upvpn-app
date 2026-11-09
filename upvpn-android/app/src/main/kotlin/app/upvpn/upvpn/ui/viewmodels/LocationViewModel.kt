package app.upvpn.upvpn.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.data.VPNRepository
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.LocationTier
import app.upvpn.upvpn.model.defaultLocation
import app.upvpn.upvpn.model.tier
import app.upvpn.upvpn.ui.state.LocationUiState
import com.github.michaelbull.result.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LocationViewModel(
    private val vpnRepository: VPNRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val tag = "LocationViewModel"

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()


    private val _recentLocations = MutableStateFlow(listOf<Location>())
    val recentLocations = _recentLocations.asStateFlow()

    // location of the VPN session in progress, if any, as known locally. It stays
    // selected even when the API no longer lists it (e.g. disabled on the backend)
    private val _sessionLocation = MutableStateFlow(null as Location?)

    private suspend fun getRecentLocations() {
        val newRecentLocations = vpnRepository.getRecentLocations(5)
        _recentLocations.update { newRecentLocations }
    }

    private suspend fun getLocations() {
        _uiState.update { value -> value.copy(isLoading = true) }
        val locations = vpnRepository.getLocations()
        locations.fold(
            success = { newLocations ->
                val sessionLocation = _sessionLocation.value
                _uiState.update { value ->
                    value.copy(
                        isLoading = false,
                        locations = newLocations,
                        // a filter on a tier that no location has anymore would show nothing
                        tierFilter = value.tierFilter
                            ?.takeIf { tier -> newLocations.any { it.tier == tier } },
                        selectedLocation = value.selectedLocation
                            ?.let { sl ->
                                // same location from newLocations, to update estimate and price
                                newLocations.firstOrNull { it.code == sl.code }
                                // not listed anymore (disabled on the backend) but a session
                                // to it is in progress: keep showing it from local information
                                    ?: sl.takeIf { sessionLocation?.code == sl.code }
                            }
                        // nothing selected (or selection no longer available): pick a default,
                        // none when there are no locations
                            ?: newLocations.defaultLocation()
                    )
                }

                // refresh recent locations from the API (estimate, price), in their order,
                // dropping those it no longer lists, as the database does
                _recentLocations.update { list ->
                    list.mapNotNull { recentLocation ->
                        newLocations.firstOrNull { it.code == recentLocation.code }
                    }
                }
            },
            failure = { error ->
                _uiState.update { value ->
                    value.copy(
                        isLoading = false,
                        locationFetchError = error
                    )
                }
            }
        )
    }

    /**
     * Locations as last received from the API, shown until the API responds: the list is not
     * empty on a fresh start, and a failure to refresh is not an empty list with an error.
     * The API response replaces them, selection and tier filter settled against it as usual.
     */
    private suspend fun getCachedLocations() {
        val cachedLocations = vpnRepository.getCachedLocations()
        if (cachedLocations.isEmpty()) {
            return
        }
        _uiState.update { value ->
            if (value.locations.isNotEmpty()) {
                // the API responded first
                value
            } else {
                value.copy(
                    locations = cachedLocations,
                    selectedLocation = value.selectedLocation ?: cachedLocations.defaultLocation()
                )
            }
        }
    }

    init {
        viewModelScope.launch(dispatcher) {
            getCachedLocations()
            getRecentLocations()
            getLocations()
        }
    }

    fun onSearchValueChange(text: String) {
        _uiState.update { value -> value.copy(search = text) }
    }

    fun clearSearchQuery() {
        _uiState.update { value -> value.copy(search = "") }
    }

    fun onRefresh() {
        viewModelScope.launch(dispatcher) {
            getLocations()
        }
    }

    fun onTierFilterChange(tier: LocationTier?) {
        _uiState.update { value -> value.copy(tierFilter = tier) }
    }

    fun onLocationSelected(location: Location) {
        _uiState.update { value -> value.copy(selectedLocation = location) }
    }

    /**
     * Location of the VPN session in progress (null when there is none). It becomes the
     * selected location, and remains so even if the API stops listing it.
     */
    fun onVpnSessionLocation(location: Location?) {
        _sessionLocation.update { location }
        if (location != null) {
            onLocationSelected(location)
        } else {
            // session over: a selection the API no longer lists (kept for the session)
            // makes way for the default location, so that it cannot be connected to again
            _uiState.update { value ->
                val selected = value.selectedLocation
                if (selected == null || value.locations.isEmpty()
                    || value.locations.any { it.code == selected.code }
                ) {
                    value
                } else {
                    value.copy(selectedLocation = value.locations.defaultLocation())
                }
            }
        }
    }

    fun addRecentLocation(location: Location) {
        _recentLocations.update { list ->
            list.filter { it.code != location.code } + location
        }
        viewModelScope.launch {
            vpnRepository.addRecentLocation(location)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "LocationViewModel::onCleared")
    }
}
