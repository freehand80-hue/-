package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSpotDao {
    @Query("SELECT * FROM parking_spots WHERE isActive = 1 ORDER BY savedAtMillis DESC LIMIT 1")
    fun observeActiveSpot(): Flow<ParkingSpotEntity?>

    @Query("SELECT * FROM parking_spots WHERE isActive = 0 ORDER BY savedAtMillis DESC LIMIT 20")
    fun observeHistorySpots(): Flow<List<ParkingSpotEntity>>

    @Query("SELECT * FROM parking_spots WHERE id = :id LIMIT 1")
    suspend fun getSpotById(id: Long): ParkingSpotEntity?

    @Query("UPDATE parking_spots SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllSpots()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: ParkingSpotEntity): Long

    @Query("UPDATE parking_spots SET floorZoneMemo = :memo WHERE id = :id")
    suspend fun updateMemo(id: Long, memo: String)

    @Query("UPDATE parking_spots SET photoPath = :photoPath WHERE id = :id")
    suspend fun updatePhotoPath(id: Long, photoPath: String?)

    @Query("DELETE FROM parking_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Long)
}
