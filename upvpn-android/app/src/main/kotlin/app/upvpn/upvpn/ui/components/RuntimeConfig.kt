package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.service.client.WgConfigKV

@Composable
@Preview
fun Preview() {
    val demoData = mapOf(
        "DNS Servers" to "1.1.1.1",
        "Handshake" to "1 min ago",
    )

    RuntimeConfig(WgConfigKV(demoData, demoData))
}

@Composable
fun KeyValueList(map: Map<String, String>) {
    Column {
        map.entries.forEachIndexed { index, entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 10.dp)
                    .sizeIn(minHeight = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (index != map.entries.size - 1) {
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun RuntimeConfig(wgConfigKV: WgConfigKV) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "INTERFACE",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Card {
                KeyValueList(wgConfigKV.interfaze)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "PEER",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Card {
                KeyValueList(wgConfigKV.peer)
            }
        }
    }
}


