package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showSystemUi = true)
@Composable
fun PreviewLocationsError() {
    LocationsError(error = "Connect Error. Please try again.", retry = {})
}

@Composable
fun LocationsError(error: String, retry: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(5.dp)
            ) {
                Icon(
                    Icons.Default.WarningAmber, contentDescription = "Warning",
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = error
                )
            }
            Button(
                onClick = retry,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "RETRY")
            }
        }
    }
}
