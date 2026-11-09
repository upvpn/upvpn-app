package app.upvpn.upvpn.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Device::class, Location::class, VpnSession::class], version = 2)
abstract class VPNDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun locationDao(): LocationDao
    abstract fun vpnSessionDao(): VpnSessionDao

    companion object {
        @Volatile
        private var Instance: VPNDatabase? = null

        /** Location gains the fields of `/api/v2/locations` so that the list can be served from it. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE location ADD COLUMN estimate INTEGER")
                db.execSQL("ALTER TABLE location ADD COLUMN pricePerGb INTEGER")
                db.execSQL("ALTER TABLE location ADD COLUMN pricePerHour INTEGER")
                db.execSQL("ALTER TABLE location ADD COLUMN priceTier TEXT")
                db.execSQL("ALTER TABLE location ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE location ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE location ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): VPNDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, VPNDatabase::class.java, "upvpn")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .enableMultiInstanceInvalidation()
                    .build()
                    .also { Instance = it }
            }
        }
    }

}
