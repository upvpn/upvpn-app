package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.ui.state.LocationUiState


@Composable
fun LocationsPopup(
    locationUiState: LocationUiState,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    showPopup: Boolean,
    dismissPopup: () -> Unit,
    onRefresh: () -> Unit,
) {
    if (showPopup) {
        Dialog(onDismissRequest = dismissPopup) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .sizeIn(minWidth = 100.dp, maxWidth = 400.dp)
            ) {
                if (locationUiState.locationFetchError != null && locationUiState.locations.isEmpty()) {
                    LocationsError(error = locationUiState.locationFetchError, onRefresh)
                } else {
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding()
                    ) {
                        locationUiState.locations.sortedBy { it.city }
                            .forEachIndexed { index, location ->
                                item {
                                    LocationComponent(
                                        location = location,
                                        isSelectedLocation = isSelectedLocation,
                                        onLocationSelected = {
                                            dismissPopup()
                                            onLocationSelected(it)
                                        },
                                        modifier = Modifier
                                    )

                                    if (index != locationUiState.locations.size - 1) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}


