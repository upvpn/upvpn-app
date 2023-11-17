package app.upvpn.upvpn.data.db

import app.upvpn.upvpn.model.DeviceInfo
import app.upvpn.upvpn.model.DeviceType
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair

fun Device.toDeviceInfo(): DeviceInfo {
    val privateKey = Key.fromBase64(this.privateKey)
    val keyPair = KeyPair(privateKey)

    return DeviceInfo(
        name = this.name,
        version = this.version,
        arch = this.arch,
        publicKey = keyPair.publicKey.toBase64(),
        uniqueId = this.uniqueId,
        deviceType = DeviceType.Android
    )
}
