package app.upvpn.upvpn.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Device::class, Location::class, VpnSession::class], version = 1)
abstract class VPNDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun locationDao(): LocationDao
    abstract fun vpnSessionDao(): VpnSessionDao

    companion object {
        @Volatile
        private var Instance: VPNDatabase? = null

        fun getDatabase(context: Context): VPNDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, VPNDatabase::class.java, "upvpn")
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { Instance = it }
            }
        }
    }

}
