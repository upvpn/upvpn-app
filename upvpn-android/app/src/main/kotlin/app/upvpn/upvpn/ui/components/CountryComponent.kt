package app.upvpn.upvpn.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.model.Country
import app.upvpn.upvpn.model.Location

@Composable
fun CountryComponent(
    isVpnSessionActivityInProgress: Boolean,
    country: Country,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = country.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
            )
            Column(
//                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                country.locations.map {
                    LocationComponent(
                        isVpnSessionActivityInProgress = isVpnSessionActivityInProgress,
                        location = it,
                        isSelectedLocation,
                        onLocationSelected
                    )
                }
            }
        }

    }
}
