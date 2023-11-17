package app.upvpn.upvpn.model

import com.wireguard.config.InetAddresses
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object Inet4AddressSerializer : KSerializer<java.net.Inet4Address> {
    override val descriptor = PrimitiveSerialDescriptor("Inet4Address", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): java.net.Inet4Address {
        return InetAddresses.parse(decoder.decodeString()) as java.net.Inet4Address
    }

    override fun serialize(encoder: Encoder, value: java.net.Inet4Address) {
        value.hostAddress?.let { encoder.encodeString(it) }
    }
}
