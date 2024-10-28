package app.upvpn.upvpn.service.client

import android.annotation.SuppressLint
import app.upvpn.upvpn.service.ipc.Event
import app.upvpn.upvpn.service.ipc.MessageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WgConfigKV(val interfaze: Map<String, String>, val peer: Map<String, String>)

private val interfaceKeys = mapOf(
    "listen_port" to "Listen Port",
    "fwmark" to "fmark"
)

private val peerKeys =
    mapOf(
        "allowed_ip" to "Allowed IPs",
        "endpoint" to "Endpoint",
        "persistent_keepalive_interval" to "Persistent keepalive",
        "last_handshake_time_sec" to "Latest handshake",
        "rx_bytes" to "Data Received",
        "tx_bytes" to "Data Sent",
    )


fun mergeMaps(pairs: List<Pair<String, String>>, mapper: Map<String, String>): Map<String, String> {
    return pairs.groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.joinToString(", ") }
        .mapKeys { (key, _) -> mapper[key] ?: key }
}

fun String.toULongOrNull(): ULong? {
    return try {
        this.toULong(radix = 10)
    } catch (e: NumberFormatException) {
        null
    }
}

fun String.toLongOrNull(): Long? {
    return try {
        this.toLong(radix = 10)
    } catch (e: NumberFormatException) {
        null
    }
}


fun wgConfigKVFromString(config: String): WgConfigKV {
    val allPairs = config.trimIndent()
        .lines()
        .filter { it.isNotBlank() }
        .map { line ->
            line.split("=", limit = 2).let { (key, value) ->
                when (key) {
                    "rx_bytes", "tx_bytes" -> {
                        key to (value.toULongOrNull()?.let { prettyBytes((it)) } ?: "")
                    }

                    "last_handshake_time_sec" -> {
                        key to (value.toLongOrNull()?.let { getTimeAgo(it) } ?: "")
                    }

                    else -> {
                        key to value
                    }
                }
            }
        }

    val interfaceMap =
        mergeMaps(
            allPairs.filter { (key, _) -> key in interfaceKeys },
            interfaceKeys
        ).toMutableMap()
    val peerMap =
        mergeMaps(
            allPairs.filter { (key, _) -> key in peerKeys },
            peerKeys
        ).toMutableMap()

    // todo: update this when these are configurable
    interfaceMap["DNS Servers"] = "1.1.1.1"

    return WgConfigKV(interfaceMap, peerMap)
}

@SuppressLint("DefaultLocale")
fun prettyBytes(bytes: ULong): String {
    return when (bytes) {
        in 0UL until 1024UL -> "$bytes B"
        in 1024UL until (1024UL * 1024UL) ->
            String.format("%.2f KiB", bytes.toDouble() / 1024)

        in (1024UL * 1024UL) until (1024UL * 1024UL * 1024UL) ->
            String.format("%.2f MiB", bytes.toDouble() / (1024 * 1024))

        in (1024UL * 1024UL * 1024UL) until (1024UL * 1024UL * 1024UL * 1024UL) ->
            String.format("%.2f GiB", bytes.toDouble() / (1024 * 1024 * 1024))

        else -> String.format("%.2f TiB", bytes.toDouble() / (1024 * 1024 * 1024 * 1024f))
    }
}

fun getTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis() / 1000 // Current time in seconds
    val timeDifference = currentTime - timestamp

    val minutes = timeDifference / 60
    val hours = timeDifference / 3600
    val days = timeDifference / 86400

    val pluralSec = if (timeDifference > 1) "s" else ""
    val pluralMin = if (minutes > 1) "s" else ""
    val pluralHour = if (hours > 1) "s" else ""
    val pluralDay = if (days > 1) "s" else ""

    return when {
        timeDifference < 60 -> "$timeDifference sec$pluralSec ago"
        timeDifference < 3600 -> "$minutes minute$pluralMin ago"
        timeDifference < 86400 -> "$hours hour$pluralHour ago"
        else -> "$days day$pluralDay ago"
    }
}

class VPNConfigManager(private val eventHandler: MessageHandler<Event>) {

    private val _wgConfig = MutableStateFlow(null as WgConfigKV?)

    val wgConfigFlow = _wgConfig.asStateFlow()

    init {
        eventHandler.registerHandler(Event.WgConfig::class) {
            onWgConfigUpdate(it.config)
        }
    }

    private fun onWgConfigUpdate(wgConfig: String?) {
        if (wgConfig != null) {
            _wgConfig.update { wgConfigKVFromString(wgConfig) }
        } else {
            _wgConfig.update { null }
        }
    }
}
