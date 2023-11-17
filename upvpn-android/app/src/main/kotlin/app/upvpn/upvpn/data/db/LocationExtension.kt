package app.upvpn.upvpn.data.db

fun Location.toModelLocation(): app.upvpn.upvpn.model.Location =
    app.upvpn.upvpn.model.Location(
        code = this.code,
        city = this.city,
        cityCode = this.cityCode,
        country = this.country,
        countryCode = this.countryCode,
        state = this.state,
        stateCode = this.stateCode
    )

fun app.upvpn.upvpn.model.Location.toDbLocation(): Location =
    Location(
        code = this.code,
        city = this.city,
        cityCode = this.cityCode,
        country = this.country,
        countryCode = this.countryCode,
        state = this.state,
        stateCode = this.stateCode
    )
