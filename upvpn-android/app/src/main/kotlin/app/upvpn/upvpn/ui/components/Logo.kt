package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.R

@Composable
fun Logo() {
    Icon(
        painterResource(R.drawable.upvpn),
        contentDescription = "UpVPN Logo",
        tint = Color.White,
        modifier = Modifier
            .fillMaxHeight(0.2f)
            .sizeIn(maxWidth = 80.dp, maxHeight = 80.dp)
            .aspectRatio(1f)
            .background(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black
            )
    )
}
