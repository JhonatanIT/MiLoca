package org.jibanez.miloca.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.jibanez.miloca.dao.LocationDao
import org.jibanez.miloca.entity.LocationPoint

@Database(entities = [LocationPoint::class], version = 2)
//@TypeConverters(LocalDateTimeConverter::class)     //TODO use type converter
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}

object DatabaseProvider {
    private var databaseInstance: LocationDatabase? = null

    fun getDatabase(context: Context): LocationDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                LocationDatabase::class.java,
                "location_database" // Use a meaningful name
            )
//                .addTypeConverter(LocalDateTimeConverter())
//                .fallbackToDestructiveMigration() // Destroys and recreates the db on schema change
                .build()

            databaseInstance = instance
            instance
        }
    }
}