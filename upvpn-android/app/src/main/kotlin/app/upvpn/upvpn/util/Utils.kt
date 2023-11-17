package app.upvpn.upvpn.util

import app.upvpn.upvpn.model.Location

fun Long.msTimerString(): String {
    val totalSeconds = this / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    val hours = totalSeconds / 3600
    val days = totalSeconds / (3600 * 24)
    return if (days == 0L) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
    }
}

fun locationForPreview(): Location {
    return Location(
        code = "usa",
        city = "Fremont",
        cityCode = "fre",
        countryCode = "US",
        country = "United States of America",
        state = "California",
        stateCode = "CA"
    )
}
