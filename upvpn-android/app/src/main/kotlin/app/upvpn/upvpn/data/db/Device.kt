package app.upvpn.upvpn.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "device")
data class Device(
    @PrimaryKey(autoGenerate = false)
    val uniqueId: UUID,
    val name: String,
    val version: String,
    val arch: String,
    val privateKey: String,
    val ipv4Address: String? = null
)
