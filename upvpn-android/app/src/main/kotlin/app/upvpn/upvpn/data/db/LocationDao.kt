package app.upvpn.upvpn.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM location")
    suspend fun getLocations(): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locations: List<Location>)

    @Query("UPDATE location SET lastAccess = :lastAccess WHERE code = :code")
    suspend fun updateLastAccess(code: String, lastAccess: Long)

    @Query("SELECT * FROM location WHERE lastAccess > 0 ORDER BY lastAccess DESC LIMIT :limit")
    fun recentLocations(limit: Int): Flow<List<Location>>
}
