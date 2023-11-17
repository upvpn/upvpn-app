package app.upvpn.upvpn.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.random
import app.upvpn.upvpn.model.search
import app.upvpn.upvpn.ui.components.AllLocations
import app.upvpn.upvpn.ui.components.SearchBar
import app.upvpn.upvpn.ui.state.LocationState
import app.upvpn.upvpn.ui.state.LocationUiState

@Preview(showSystemUi = true)
@Composable
fun PreviewAlLLocationsWithSearch() {
    val locations = listOf<Location>().random(10)
    AllLocationsWithSearch(
        isVpnSessionActivityInProgress = false,
        locationState = LocationState.Locations(locations),
        searchText = "a",
        onSearchValueChange = {},
        onRefresh = {},
        clearSearchQuery = {},
        isSelectedLocation = { it == locations.first() },
        onLocationSelected = {})
}


@Composable
fun LocationScreen(
    isVpnSessionActivityInProgress: Boolean,
    uiState: LocationUiState,
    onSearchValueChange: (String) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {
    AllLocationsWithSearch(
        isVpnSessionActivityInProgress = isVpnSessionActivityInProgress,
        locationState = uiState.locationState,
        searchText = uiState.search,
        onSearchValueChange = onSearchValueChange,
        onRefresh = onRefresh,
        clearSearchQuery = clearSearchQuery,
        isSelectedLocation = isSelectedLocation,
        onLocationSelected = onLocationSelected
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllLocationsWithSearch(
    isVpnSessionActivityInProgress: Boolean,
    locationState: LocationState,
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {

    val searchedLocationState = when (locationState) {
        is LocationState.Loading -> locationState
        is LocationState.Error -> locationState
        is LocationState.Locations -> {
            if (searchText.isEmpty()) {
                locationState
            } else {
                LocationState.Locations(
                    locationState.locations.search(searchText)
                )
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            // so that scrolling on location screen appears without gap on bottom
            .padding(20.dp, 20.dp, 20.dp, 0.dp)
            .fillMaxSize()
    ) {
        SearchBar(
            searchText = searchText,
            onSearchValueChange = onSearchValueChange,
            clearSearchQuery = clearSearchQuery
        )

        AllLocations(
            isVpnSessionActivityInProgress = isVpnSessionActivityInProgress,
            locationState = searchedLocationState,
            verticalCountrySpacing = 10.dp,
            onRefresh = onRefresh,
            isSelectedLocation = isSelectedLocation,
            onLocationSelected = onLocationSelected
        )
    }
}
