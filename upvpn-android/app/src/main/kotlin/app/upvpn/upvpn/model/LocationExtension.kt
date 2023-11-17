package app.upvpn.upvpn.model

fun List<Location>.toCountries(): List<Country> =
    this.sortedWith(compareByDescending<Location> { it.country }.thenBy { it.city })
        .groupBy { it.country }
        .map { Country(it.key, it.value) }

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
        )
    }

fun Location.displayText(): String {
    return when (countryCode.uppercase()) {
        "US", "CA" -> {
            when (stateCode) {
                null -> city.uppercase()
                else -> "${city.uppercase()}, ${stateCode.uppercase()}"
            }
        }
        else -> {
            city.uppercase()
        }
    }
}
