package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.model.LOCATION_COLD_COLOR
import app.upvpn.upvpn.model.LOCATION_WARM_COLOR
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.displayText


@Composable
fun LocationComponent(
    location: Location,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = isSelectedLocation(location)
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                onClick = { onLocationSelected(location) },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary)
            )
            .defaultMinSize(minHeight = 68.dp)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CountryIcon(
            countryCode = location.countryCode,
            modifier = Modifier.size(width = 22.dp, height = 16.dp)
        )

        Text(
            text = location.displayText(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircleOutline,
                contentDescription = "selected",
                tint = LOCATION_WARM_COLOR,
                modifier = Modifier.size(20.dp)
            )
        }

        Icon(
            imageVector = Icons.Rounded.Circle,
            contentDescription = "Warm or Cold",
            tint = location.estimate?.let { if (it <= 10) LOCATION_WARM_COLOR else LOCATION_COLD_COLOR }
                ?: LOCATION_COLD_COLOR,
            modifier = Modifier.size(12.dp)
        )
    }
}
