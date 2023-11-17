package app.upvpn.upvpn.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT * from user LIMIT 1")
    suspend fun getUser(): User?

    @Query("SELECT * from user LIMIT 1")
    fun getUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)
}
