package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.model.Location


@Composable
fun LocationComponent(
    isVpnSessionActivityInProgress: Boolean,
    location: Location,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
//            .background(Color.Transparent)
            .fillMaxWidth()
            .clickable(
                onClick = { onLocationSelected(location) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CountryIcon(countryCode = location.countryCode, Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp))

        Text(text = location.city, modifier = Modifier.weight(1f))

        RadioButton(
            enabled = isVpnSessionActivityInProgress.not(),
            selected = isSelectedLocation(location),
            onClick = { onLocationSelected(location) }
        )
    }
}
