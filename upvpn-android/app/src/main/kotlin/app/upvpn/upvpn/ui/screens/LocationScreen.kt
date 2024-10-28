package app.upvpn.upvpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.random
import app.upvpn.upvpn.model.search
import app.upvpn.upvpn.ui.components.AllLocations
import app.upvpn.upvpn.ui.components.SearchBar
import app.upvpn.upvpn.ui.state.LocationUiState

@Preview(showSystemUi = true)
@Composable
fun PreviewAlLLocationsWithSearch() {
    val locations = listOf<Location>().random(10)
    AllLocationsWithSearch(
        locationUiState = LocationUiState(locations),
        searchText = "a",
        onSearchValueChange = {},
        onRefresh = {},
        clearSearchQuery = {},
        isSelectedLocation = { it == locations.first() },
        onLocationSelected = {})
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    uiState: LocationUiState,
    onSearchValueChange: (String) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {
    val state = rememberPullToRefreshState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    // reload locations whenever screen show
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            onRefresh()
        }
    }

    PullToRefreshBox(
        state = state,
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh
    ) {

        AllLocationsWithSearch(
            locationUiState = uiState,
            searchText = uiState.search,
            onSearchValueChange = onSearchValueChange,
            onRefresh = onRefresh,
            clearSearchQuery = clearSearchQuery,
            isSelectedLocation = isSelectedLocation,
            onLocationSelected = onLocationSelected
        )
    }
}

@Composable
fun AllLocationsWithSearch(
    locationUiState: LocationUiState,
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {
    val searchedLocationUiState = LocationUiState(
        locations = if (searchText.isEmpty()) locationUiState.locations else locationUiState.locations.search(
            searchText
        ),
        search = locationUiState.search,
        selectedLocation = locationUiState.selectedLocation,
        isLoading = locationUiState.isLoading,
        locationFetchError = locationUiState.locationFetchError
    )

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
            locationUiState = searchedLocationUiState,
            verticalCountrySpacing = 10.dp,
            onRefresh = onRefresh,
            isSelectedLocation = isSelectedLocation,
            onLocationSelected = onLocationSelected
        )
    }
}
