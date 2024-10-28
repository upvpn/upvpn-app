package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Circle
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
import app.upvpn.upvpn.model.DEFAULT_LOCATION
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.displayText
import app.upvpn.upvpn.model.warmOrColdColor
import app.upvpn.upvpn.model.warmOrColdDescription
import app.upvpn.upvpn.ui.state.VpnUiState
import app.upvpn.upvpn.ui.state.isConnectedOrDisconnectedOrDisconnecting

@Composable
fun LocationSelector(
    selectedLocation: Location?,
    vpnUiState: VpnUiState,
    onLocationSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val location = selectedLocation ?: DEFAULT_LOCATION

    Button(
        onClick = onLocationSelectorClick,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CountryIcon(countryCode = selectedLocation?.countryCode ?: "US")
            Text(
                text = location.displayText(),
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Visible
            )
            if (vpnUiState.isConnectedOrDisconnectedOrDisconnecting()) {
                Icon(
                    imageVector = Icons.Rounded.Circle,
                    contentDescription = location.warmOrColdDescription(),
                    modifier = Modifier.size(15.dp),
                    tint = location.warmOrColdColor()
                )
            } else {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
