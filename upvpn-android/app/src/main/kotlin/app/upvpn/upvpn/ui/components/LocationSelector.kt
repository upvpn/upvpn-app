package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.displayText
import app.upvpn.upvpn.ui.state.LocationState
import app.upvpn.upvpn.ui.state.VpnUiState
import app.upvpn.upvpn.ui.state.locationSelectorEnabled

@Composable
fun LocationSelector(
    selectedLocation: Location?,
    locationState: LocationState,
    vpnUiState: VpnUiState,
    openLocationScreen: () -> Unit,
    reloadLocations: () -> Unit,
    modifier: Modifier = Modifier
) {

    val (displayText, icon, onClick) = when (locationState) {
        is LocationState.Loading -> Triple(
            "Loading",
            Icons.Rounded.ChevronRight,
            openLocationScreen
        )

        is LocationState.Error -> Triple("No Locations", Icons.Rounded.Refresh, reloadLocations)
        is LocationState.Locations -> {
            if (selectedLocation == null) {
                if (locationState.locations.isEmpty()) {
                    Triple("No Locations", Icons.Rounded.Refresh, reloadLocations)
                } else {
                    val found =
                        locationState.locations.firstOrNull { it.city.contains("ashburn") }
                    if (found != null) {
                        Triple(found.displayText(), Icons.Rounded.ChevronRight, openLocationScreen)
                    } else {
                        Triple(
                            locationState.locations.first().displayText(),
                            Icons.Rounded.ChevronRight,
                            openLocationScreen
                        )
                    }
                }
            } else {
                Triple(
                    selectedLocation.displayText(),
                    Icons.Rounded.ChevronRight,
                    openLocationScreen
                )
            }
        }
    }

    val isLoading = vpnUiState is VpnUiState.Checking || locationState is LocationState.Loading

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = vpnUiState.locationSelectorEnabled() && locationState.locationSelectorEnabled(),
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .aspectRatio(1f)
                )
            }
            if (displayText.contains("No Locations").not()) {
                CountryIcon(countryCode = selectedLocation?.countryCode ?: "US")
            }
            Text(text = displayText, overflow = TextOverflow.Visible)
            if (isLoading.not()) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Arrow"
                )
            }
        }
    }
}