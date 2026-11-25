package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ParkingSpotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun parkingSpotDao(): ParkingSpotDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null

        fun getInstance(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "my_car_spot.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
