package app.upvpn.upvpn.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface VpnSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vpnSession: VpnSession)

    @Update
    suspend fun update(vpnSession: VpnSession)

    @Delete
    suspend fun delete(vpnSession: VpnSession)

    @Query("SELECT * from vpn_session where requestId = :requestId")
    suspend fun withRequestId(requestId: UUID): VpnSession?

    @Query("DELETE from vpn_session")
    suspend fun deleteAll()

    @Query("DELETE from vpn_session where requestId = :requestId")
    suspend fun deleteWithRequestId(requestId: UUID)

    @Query("UPDATE vpn_session set markForDeletion = 'true'")
    suspend fun markAllForDeletion()

    @Query("SELECT requestId from vpn_session where markForDeletion = 'true'")
    suspend fun toReclaim(): List<UUID>
}
