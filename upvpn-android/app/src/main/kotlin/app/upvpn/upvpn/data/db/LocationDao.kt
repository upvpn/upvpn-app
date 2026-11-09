package app.upvpn.upvpn.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LocationDao {

    @Query("SELECT * FROM location")
    suspend fun getLocations(): List<Location>

    /** Inserts new locations and overwrites existing ones, [Location.lastAccess] included. */
    @Upsert
    suspend fun upsert(locations: List<Location>)

    @Query("DELETE FROM location WHERE code NOT IN (:notInLocationCodes)")
    suspend fun deleteNotIn(notInLocationCodes: List<String>);

    @Query("UPDATE location SET lastAccess = :lastAccess WHERE code = :code")
    suspend fun updateLastAccess(code: String, lastAccess: Long)

    @Query("SELECT * FROM location WHERE lastAccess > 0 ORDER BY lastAccess ASC LIMIT :limit")
    suspend fun recentLocations(limit: Int): List<Location>
}
