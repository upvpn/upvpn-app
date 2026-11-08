package app.upvpn.upvpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.LocationTier
import app.upvpn.upvpn.model.availableTiers
import app.upvpn.upvpn.model.displayName
import app.upvpn.upvpn.model.random
import app.upvpn.upvpn.model.search
import app.upvpn.upvpn.model.withTier
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
        onTierFilterChange = {},
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
    onTierFilterChange: (LocationTier?) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {
    val state = rememberPullToRefreshState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    // tracks pull-to-refresh UI separately from data loading state so that
    // auto refresh on screen show doesn't display the pull-to-refresh spinner
    var userInitiatedRefresh by remember { mutableStateOf(false) }

    // reload locations whenever screen show
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            onRefresh()
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            userInitiatedRefresh = false
        }
    }

    PullToRefreshBox(
        state = state,
        isRefreshing = userInitiatedRefresh && uiState.isLoading,
        onRefresh = {
            userInitiatedRefresh = true
            onRefresh()
        }
    ) {

        AllLocationsWithSearch(
            locationUiState = uiState,
            searchText = uiState.search,
            onSearchValueChange = onSearchValueChange,
            onTierFilterChange = onTierFilterChange,
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
    onTierFilterChange: (LocationTier?) -> Unit,
    onRefresh: () -> Unit,
    clearSearchQuery: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
) {
    val filteredLocations = locationUiState.locations
        .withTier(locationUiState.tierFilter)
        .search(searchText)

    val searchedLocationUiState = locationUiState.copy(locations = filteredLocations)

    // offer a tier filter only when there is something to filter by
    val tiers = locationUiState.locations.availableTiers()

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            // so that scrolling on location screen appears without gap on bottom
            .padding(20.dp, 20.dp, 20.dp, 0.dp)
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SearchBar(
                searchText = searchText,
                onSearchValueChange = onSearchValueChange,
                clearSearchQuery = clearSearchQuery,
                modifier = Modifier.weight(1f)
            )
            if (tiers.size > 1) {
                TierFilterMenu(
                    tiers = tiers,
                    selectedTier = locationUiState.tierFilter,
                    onTierSelected = onTierFilterChange,
                )
            }
        }

        if (filteredLocations.isEmpty() && locationUiState.locations.isNotEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No locations match",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AllLocations(
                locationUiState = searchedLocationUiState,
                verticalCountrySpacing = 16.dp,
                onRefresh = onRefresh,
                isSelectedLocation = isSelectedLocation,
                onLocationSelected = onLocationSelected
            )
        }
    }
}

/**
 * Filter of locations by tier, next to the search bar: a menu offering "All" and each of
 * [tiers]. The icon is filled while a tier is selected.
 */
@Composable
fun TierFilterMenu(
    tiers: List<LocationTier>,
    selectedTier: LocationTier?,
    onTierSelected: (LocationTier?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            if (selectedTier == null) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = "Filter by tier"
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.FilterAlt,
                    contentDescription = "Filtered by ${selectedTier.displayName()}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TierFilterMenuItem(
                label = "All",
                selected = selectedTier == null,
                onClick = {
                    onTierSelected(null)
                    expanded = false
                },
            )
            tiers.forEach { tier ->
                TierFilterMenuItem(
                    label = tier.displayName(),
                    selected = selectedTier == tier,
                    onClick = {
                        onTierSelected(tier)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TierFilterMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        // labels stay aligned on the left; the check mark of the selected one is on the right
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "selected",
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        },
    )
}
