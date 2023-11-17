package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.R
import app.upvpn.upvpn.data.CountryMap

@Composable
fun CountryIcon(countryCode: String, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(
            id = CountryMap.resource.getOrDefault(
                countryCode.lowercase(),
                R.drawable.upvpn
            )
        ),
        contentDescription = countryCode,
        tint = Color.Unspecified,
        modifier = modifier
            .clip(shape = RoundedCornerShape(2.dp))
    )
}
