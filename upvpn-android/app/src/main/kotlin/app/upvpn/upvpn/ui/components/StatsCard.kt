package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.service.client.WgConfigKV

@Composable
fun Stats(value: String, title: String, isDownload: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isDownload) Icons.Default.ArrowCircleDown else Icons.Default.ArrowCircleUp,
            "Downloaded", modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.size(2.dp))
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsCard(wgConfigKV: WgConfigKV?) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showBottomSheet = !showBottomSheet }
    ) {
        Stats(wgConfigKV?.peer?.get("Data Received") ?: "-", "DOWNLOADED", isDownload = true)
        VerticalDivider(modifier = Modifier.padding(vertical = 15.dp))
        Stats(wgConfigKV?.peer?.get("Data Sent") ?: "-", "UPLOADED")
    }

    if (showBottomSheet && wgConfigKV != null) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            RuntimeConfig(wgConfigKV)
        }
    }
}


@Preview
@Composable
fun PreviewStats() {
    Stats("12.23KiB", "DOWNLOADED")
}

@Preview(showSystemUi = true)
@Composable
fun PreviewStatsCard() {
    StatsCard(null)
}
