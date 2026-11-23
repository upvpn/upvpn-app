package app.upvpn.upvpn.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A location as last received from the API, so that the list can be shown before (or
 * without) a response from it, and recent locations keep their price and estimate.
 * [lastAccess] is local: when the user last connected to it, for recent locations.
 */
@Entity(tableName = "location")
data class Location(
    @PrimaryKey(autoGenerate = false)
    val code: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val cityCode: String,
    val state: String? = null,
    val stateCode: String? = null,
    val lastAccess: Int? = null,
    val estimate: Short? = null,
    // price is present when priceTier is
    val pricePerGb: Int? = null,
    val pricePerHour: Int? = null,
    val priceTier: String? = null,
    // geo is present when both are
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean = false,
)
