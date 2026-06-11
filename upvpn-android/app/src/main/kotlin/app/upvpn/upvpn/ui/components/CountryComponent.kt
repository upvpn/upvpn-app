package app.upvpn.upvpn.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.model.Country
import app.upvpn.upvpn.model.Location

@Composable
fun CountryComponent(
    country: Country,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Text(
            text = country.name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 4.dp)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                country.locations.forEachIndexed { index, location ->
                    LocationComponent(
                        location = location,
                        isSelectedLocation = isSelectedLocation,
                        onLocationSelected = onLocationSelected
                    )
                    if (index < country.locations.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 52.dp)
                        )
                    }
                }
            }
        }
    }
}
