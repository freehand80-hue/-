package com.example.data.repository

import com.example.data.local.ParkingSpotDao
import com.example.data.local.ParkingSpotEntity
import kotlinx.coroutines.flow.Flow

class ParkingRepository(private val dao: ParkingSpotDao) {
    val activeSpot: Flow<ParkingSpotEntity?> = dao.observeActiveSpot()
    val historySpots: Flow<List<ParkingSpotEntity>> = dao.observeHistorySpots()

    suspend fun saveNewParkingSpot(
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float?,
        savedAtMillis: Long,
        floorZoneMemo: String,
        photoPath: String?
    ): Long {
        dao.deactivateAllSpots()
        return dao.insertSpot(
            ParkingSpotEntity(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracyMeters,
                savedAtMillis = savedAtMillis,
                floorZoneMemo = floorZoneMemo.trim(),
                photoPath = photoPath,
                isActive = true
            )
        )
    }

    suspend fun updateMemo(spotId: Long, memo: String) {
        dao.updateMemo(spotId, memo.trim())
    }

    suspend fun updatePhotoPath(spotId: Long, photoPath: String?) {
        dao.updatePhotoPath(spotId, photoPath)
    }

    suspend fun clearActiveParking() {
        dao.deactivateAllSpots()
    }

    suspend fun deleteHistorySpot(spotId: Long): String? {
        val spot = dao.getSpotById(spotId)
        dao.deleteSpotById(spotId)
        return spot?.photoPath
    }
}
