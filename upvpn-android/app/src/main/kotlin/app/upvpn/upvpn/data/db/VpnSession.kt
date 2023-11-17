package app.upvpn.upvpn.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.util.UUID

@Entity(tableName = "vpn_session")
data class VpnSession(
    @PrimaryKey(autoGenerate = false)
    val requestId: UUID,
    val code: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val cityCode: String,
    val state: String? = null,
    val stateCode: String? = null,
    val serverStatus: String? = null,
    val sessionUUID: UUID? = null,
    val serverIpv4Endpoint: String? = null,
    val serverPrivateIpv4: String? = null,
    val serverPublicKey: String? = null,
    val markForDeletion: Boolean = false
) {
    constructor(requestId: UUID, location: Location) :
            this(
                requestId = requestId,
                code = location.code,
                country = location.country,
                countryCode = location.countryCode,
                city = location.city,
                cityCode = location.cityCode,
                state = location.state,
                stateCode = location.stateCode,
            ) {
    }
}

