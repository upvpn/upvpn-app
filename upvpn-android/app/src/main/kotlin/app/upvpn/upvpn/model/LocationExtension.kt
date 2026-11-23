package app.upvpn.upvpn.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun List<Location>.toCountries(): List<Country> =
    this.sortedWith(compareByDescending<Location> { it.country }.thenBy { it.city })
        .groupBy { it.country }
        .map { Country(it.key, it.value) }

/**
 * The location to select when the user has not chosen one yet: any of the locations the
 * API flags as default, at random when there are several; the first location when the
 * list has no default.
 */
fun List<Location>.defaultLocation(): Location? =
    this.filter { it.isDefault }.randomOrNull() ?: this.firstOrNull()

val Location.tier: LocationTier?
    get() = this.price?.tier

/** Premium location indicator, explained on the help screen. */
const val PREMIUM_INDICATOR = "\uD83D\uDCB0"

fun LocationTier.displayName(): String = when (this) {
    LocationTier.Free -> "Free"
    LocationTier.Standard -> "Standard"
    LocationTier.Premium -> "Premium"
}

/** The tiers present in this list, in display order, so that a filter offers only those. */
fun List<Location>.availableTiers(): List<LocationTier> =
    LocationTier.entries.filter { tier -> this.any { it.tier == tier } }

/** Locations of [tier]; all of them when [tier] is null. */
fun List<Location>.withTier(tier: LocationTier?): List<Location> =
    if (tier == null) this else this.filter { it.tier == tier }

fun List<Location>.search(query: String): List<Location> {
    val q = query.lowercase()
    return this.filter {
        query.isEmpty() ||
                (query.isNotEmpty() && (
                        it.city.lowercase().contains(q) || it.state?.lowercase()
                            ?.contains(q) ?: false
                                || it.country.lowercase().contains(q)
                                || it.cityCode.lowercase().contains(q)
                                || it.stateCode?.lowercase()?.contains(q) ?: false
                                || it.countryCode.lowercase().contains(q)
                        ))
    }
}

private fun randomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun List<Location>.random(length: Int): List<Location> = (1..length)
    .map {
        Location(
            code = randomString(10),
            city = randomString(10),
            cityCode = randomString(2),
            country = randomString(10),
            countryCode = "US",
            price = Price(perGb = 100, perHour = 1, tier = LocationTier.entries.random()),
        )
    }

fun Location.displayText(): String {
    return when (countryCode.uppercase()) {
        "US", "CA" -> {
            when (stateCode) {
                null -> city
                else -> "${city}, ${stateCode.uppercase()}"
            }
        }

        else -> {
            city
        }
    }
}

val LOCATION_WARM_COLOR = Color(22, 163, 74, 255)
val LOCATION_COLD_COLOR = Color(56, 189, 248, 255)
val LOCATION_INDICATOR_SIZE = 15.dp

fun Location.warmOrColdColor(): Color {
    return when (this.estimate) {
        null -> LOCATION_COLD_COLOR
        else -> {
            when (this.estimate <= 10) {
                true -> LOCATION_WARM_COLOR
                else -> LOCATION_COLD_COLOR
            }
        }
    }
}

fun Location.warmOrColdDescription(): String {
    return when (this.estimate) {
        null -> ""
        else -> {
            when (this.estimate <= 10) {
                true -> "Warm"
                else -> "Cold"
            }
        }
    }
}
