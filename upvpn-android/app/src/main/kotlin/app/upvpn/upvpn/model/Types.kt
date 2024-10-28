package app.upvpn.upvpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue

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
    val estimate: Short? = null
) : Parcelable


val DEFAULT_LOCATION = Location(
    code = "us_va_ashburn",
    country = "United States of America",
    countryCode = "US",
    city = "Ashburn",
    cityCode = "ash",
    state = "Virginia",
    stateCode = "VA",
    estimate = null
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

fun UserPlanPayAsYouGo.prettyBalance(): String {
    val sign = if (this.balance < 0) "-" else ""
    return "$sign$%.2f".format(this.balance.absoluteValue / 100.0)
}

@Serializable
sealed class UserPlan {
    @Serializable
    @SerialName("PayAsYouGo")
    data class PayAsYouGo(val content: UserPlanPayAsYouGo) : UserPlan()

    @Serializable
    @SerialName("AnnualSubscription")
    data object AnnualSubscription : UserPlan()
}

fun UserPlan.forDisplay(): String {
    return when (this) {
        is UserPlan.PayAsYouGo -> "Pay as you go"
        is UserPlan.AnnualSubscription -> "Yearly plan"
    }
}
