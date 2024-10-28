package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
    Row(
        modifier = modifier
            .sizeIn(minHeight = 60.dp)
            .clip(RoundedCornerShape(5.dp))
            .fillMaxWidth()
            .clickable(
                onClick = { onLocationSelected(location) },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CountryIcon(countryCode = location.countryCode, Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp))

        Text(
            text = location.displayText(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        if (isSelectedLocation(location)) {
            Icon(
                imageVector = Icons.Rounded.CheckCircleOutline,
                contentDescription = "selected",
                tint = LOCATION_WARM_COLOR,
                modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
            )
        }

        Icon(
            imageVector = Icons.Rounded.Circle,
            contentDescription = "Warm or Cold",
            tint = location.estimate?.let { if (it <= 10) LOCATION_WARM_COLOR else LOCATION_COLD_COLOR }
                ?: LOCATION_COLD_COLOR,
            modifier = Modifier
                .size(15.dp)
        )

        Spacer(modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
    }
}
