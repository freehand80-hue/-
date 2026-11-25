package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spots")
data class ParkingSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val savedAtMillis: Long,
    val floorZoneMemo: String = "",
    val photoPath: String? = null,
    val isActive: Boolean = true
)
