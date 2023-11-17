package app.upvpn.upvpn.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val lastAccess: Int? = null
)
