package app.upvpn.upvpn.data.db

import app.upvpn.upvpn.model.Geo
import app.upvpn.upvpn.model.LocationTier
import app.upvpn.upvpn.model.Price

fun Location.toModelLocation(): app.upvpn.upvpn.model.Location =
    app.upvpn.upvpn.model.Location(
        code = this.code,
        city = this.city,
        cityCode = this.cityCode,
        country = this.country,
        countryCode = this.countryCode,
        state = this.state,
        stateCode = this.stateCode,
        estimate = this.estimate,
        price = this.priceTier?.let { tierName ->
            Price(
                perGb = this.pricePerGb ?: 0,
                perHour = this.pricePerHour ?: 0,
                // a tier this version does not know (stored by a newer one) falls back
                // to Standard, as decoding the API response does
                tier = LocationTier.entries.firstOrNull { it.name == tierName }
                    ?: LocationTier.Standard
            )
        },
        geo = this.latitude?.let { latitude ->
            this.longitude?.let { longitude -> Geo(latitude, longitude) }
        },
        isDefault = this.isDefault,
    )

fun app.upvpn.upvpn.model.Location.toDbLocation(): Location =
    Location(
        code = this.code,
        city = this.city,
        cityCode = this.cityCode,
        country = this.country,
        countryCode = this.countryCode,
        state = this.state,
        stateCode = this.stateCode,
        estimate = this.estimate,
        pricePerGb = this.price?.perGb,
        pricePerHour = this.price?.perHour,
        priceTier = this.price?.tier?.name,
        latitude = this.geo?.latitude,
        longitude = this.geo?.longitude,
        isDefault = this.isDefault,
    )
