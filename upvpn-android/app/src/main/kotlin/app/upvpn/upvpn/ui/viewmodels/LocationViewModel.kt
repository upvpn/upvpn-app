package app.upvpn.upvpn.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.data.VPNRepository
import app.upvpn.upvpn.model.DEFAULT_LOCATION
import app.upvpn.upvpn.model.Location
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

    private suspend fun getRecentLocations() {
        val newRecentLocations = vpnRepository.getRecentLocations(5)
        _recentLocations.update { newRecentLocations }
    }

    private suspend fun getLocations() {
        _uiState.update { value -> value.copy(isLoading = true) }
        val locations = vpnRepository.getLocations()
        locations.fold(
            success = { newLocations ->
                _uiState.update { value ->
                    value.copy(
                        isLoading = false,
                        locations = newLocations,
                        // to update estimates
                        selectedLocation = value.selectedLocation
                            // find same location as selected from newLocations
                            ?.let { sl -> newLocations.firstOrNull { it.code == sl.code } }
                            ?: /* now try computing default selected location */
                            (newLocations.find {
                                it.city.lowercase().contains("ashburn")
                            } ?: DEFAULT_LOCATION)
                    )
                }

                // make sure to preserve recent locations order
                _recentLocations.update { list ->
                    list.map { recentLocation ->
                        newLocations.firstOrNull { it.code == recentLocation.code }
                            ?: recentLocation
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

    init {
        viewModelScope.launch(dispatcher) {
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

    fun onLocationSelected(location: Location) {
        _uiState.update { value -> value.copy(selectedLocation = location) }
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
