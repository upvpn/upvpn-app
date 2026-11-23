package app.upvpn.upvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.absoluteValue
import java.util.Locale

@Serializable
data class UserCredentials(
    val email: String,
    val password: String
)

@Serializable
data class UserCredentialsWithCode(
    val email: String,
    val password: String,
    val code: String,
)

@Serializable
data class OnlyEmail(
    val email: String,
)

@Serializable
enum class DeviceType(val string: String) {
    @SerialName("linux")
    Linux("linux"),

    @SerialName("macos")
    MacOS("macos"),

    @SerialName("windows")
    Windows("windows"),

    @SerialName("ios")
    IOS("ios"),

    @SerialName("android")
    Android("android"),
}

@Serializable
data class DeviceInfo(
    val name: String,
    val version: String,
    val arch: String,
    val publicKey: String,
    @Serializable(with = UUIDSerializer::class)
    val uniqueId: java.util.UUID,
    val deviceType: DeviceType
)

@Serializable
data class AddDeviceRequest(
    val userCreds: UserCredentials,
    val deviceInfo: DeviceInfo
)

@Serializable
data class DeviceAddresses(
    @Serializable(with = Inet4AddressSerializer::class)
    val ipv4Address: java.net.Inet4Address
)

@Serializable
data class AddDeviceResponse(
    val token: String,
    val deviceAddresses: DeviceAddresses
)

/**
 * Who pays for a session at a location.
 *
 * - [Free]: never charged, subject to the free usage limits
 * - [Standard]: covered by a yearly plan, otherwise billed from the pay as you go balance
 * - [Premium]: always billed from the pay as you go balance, whatever the plan
 */
@Serializable
enum class LocationTier {
    @SerialName("free")
    Free,

    @SerialName("standard")
    Standard,

    @SerialName("premium")
    Premium,
}

/** The current price of a location, in cents. */
@Parcelize
@Serializable
data class Price(
    val perGb: Int = 0,
    val perHour: Int = 0,
    // default so that a tier added to the API later is coerced to Standard
    // (see coerceInputValues in the Json config) instead of failing to decode
    val tier: LocationTier = LocationTier.Standard,
) : Parcelable

/** The geographical pin of a location. */
@Parcelize
@Serializable
data class Geo(
    val latitude: Double,
    val longitude: Double,
) : Parcelable

/**
 * A location from `/api/v2/locations`.
 *
 * Every field the v2 API added over v1 has a default, so a location restored from
 * local storage (recent locations, an in-progress session) still decodes without
 * them. Unknown keys are ignored by the Json config, so the API can grow freely.
 */
@Parcelize
@Serializable
data class Location(
    val code: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val cityCode: String,
    val state: String? = null,
    val stateCode: String? = null,
    val estimate: Short? = null,
    val price: Price? = null,
    val geo: Geo? = null,
    val isDefault: Boolean = false,
) : Parcelable

/**
 * `/api/v2/locations` response: an object rather than a bare list so that fields can be
 * added next to [locations] later without changing the shape.
 */
@Serializable
data class LocationsV2(
    val locations: List<Location> = listOf(),
)


@Serializable
data class NewSession(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val deviceUniqueId: java.util.UUID,
    val locationCode: String
)

@Serializable
data class EndSessionApi(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val deviceUniqueId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID? = null,
    val reason: String
)

@Serializable
data class ClientConnected(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val deviceUniqueId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
data class Accepted(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
data class ServerCreated(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
data class ServerRunning(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
data class Failed(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
data class ServerReady(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID,
    val publicKey: String,
    val ipv4Endpoint: String, // TODO: socketaddr
    @Serializable(with = Inet4AddressSerializer::class)
    val privateIpv4: java.net.Inet4Address
)

@Serializable
data class Ended(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID,
    val reason: String
)

@Serializable
data class VpnSessionStatusRequest(
    @Serializable(with = UUIDSerializer::class)
    val requestId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val deviceUniqueId: java.util.UUID,
    @Serializable(with = UUIDSerializer::class)
    val vpnSessionUuid: java.util.UUID
)

@Serializable
sealed class VpnSessionStatus {
    @Serializable
    @SerialName("accepted")
    data class Accepted(val content: app.upvpn.upvpn.model.Accepted) : VpnSessionStatus()

    @Serializable
    @SerialName("failed")
    data class Failed(val content: app.upvpn.upvpn.model.Failed) : VpnSessionStatus()

    @Serializable
    @SerialName("serverCreated")
    data class ServerCreated(val content: app.upvpn.upvpn.model.ServerCreated) : VpnSessionStatus()

    @Serializable
    @SerialName("serverRunning")
    data class ServerRunning(val content: app.upvpn.upvpn.model.ServerRunning) : VpnSessionStatus()

    @Serializable
    @SerialName("serverReady")
    data class ServerReady(val content: app.upvpn.upvpn.model.ServerReady) : VpnSessionStatus()

    @Serializable
    @SerialName("clientConnected")
    data class ClientConnected(val content: app.upvpn.upvpn.model.ClientConnected) :
        VpnSessionStatus()

    @Serializable
    @SerialName("ended")
    data class Ended(val content: app.upvpn.upvpn.model.Ended) : VpnSessionStatus()
}

@Serializable
data class UserPlanPayAsYouGo(
    val balance: Int
)

@Serializable
data class UserPlanAnnualSubscription(
    /** the pay as you go balance in cents, which pays for premium locations */
    val balance: Int = 0,
    /** when the subscription started, ISO 8601 */
    val startedAt: String? = null,
)

/** [UserPlanAnnualSubscription.startedAt] as a local date for display, null when missing or unparsable. */
fun UserPlanAnnualSubscription.prettyStartedAt(): String? {
    val startedAt = this.startedAt ?: return null
    val instant = runCatching { OffsetDateTime.parse(startedAt).toInstant() }.getOrNull()
        ?: runCatching { Instant.parse(startedAt) }.getOrNull()
        ?: return null
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

// the balance is in USD, so format it US style ("$4.99") regardless of the device locale
fun Int.prettyCents(): String {
    val sign = if (this < 0) "-" else ""
    return "$sign$%.2f".format(Locale.US, this.absoluteValue / 100.0)
}

fun UserPlanPayAsYouGo.prettyBalance(): String = this.balance.prettyCents()

@Serializable
sealed class UserPlan {
    @Serializable
    @SerialName("PayAsYouGo")
    data class PayAsYouGo(val content: UserPlanPayAsYouGo) : UserPlan()

    @Serializable
    @SerialName("AnnualSubscription")
    data class AnnualSubscription(val content: UserPlanAnnualSubscription) : UserPlan()
}

fun UserPlan.forDisplay(): String {
    return when (this) {
        is UserPlan.PayAsYouGo -> "Pay as you go"
        is UserPlan.AnnualSubscription -> "Yearly plan"
    }
}

/** The pay as you go balance in cents, which every plan carries (it pays for premium locations). */
fun UserPlan.balance(): Int {
    return when (this) {
        is UserPlan.PayAsYouGo -> this.content.balance
        is UserPlan.AnnualSubscription -> this.content.balance
    }
}

fun UserPlan.prettyBalance(): String = this.balance().prettyCents()

@Serializable
data class SsoCredentials(val provider: String, val idToken: String)

@Serializable
data class SsoAddDeviceRequest(val ssoCredentials: SsoCredentials, val deviceInfo: DeviceInfo)
