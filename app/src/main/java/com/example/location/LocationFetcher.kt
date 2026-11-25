package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class CapturedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?
)

class LocationFetcher(private val context: Context) {
    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): Result<CapturedLocation> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }

        // 1. Try fresh high-accuracy fix via FusedLocationProviderClient with timeout
        val freshLocation = withTimeoutOrNull(8_000L) {
            getCurrentFusedLocation(Priority.PRIORITY_HIGH_ACCURACY)
        }
        if (freshLocation != null) {
            return@withContext Result.success(freshLocation.toCaptured())
        }

        // 2. Try balanced power current fix (faster indoors / underground fringe)
        val balancedLocation = withTimeoutOrNull(4_000L) {
            getCurrentFusedLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        }
        if (balancedLocation != null) {
            return@withContext Result.success(balancedLocation.toCaptured())
        }

        // 3. Fallback to Fused last known location
        val fusedLast = withTimeoutOrNull(2_000L) { getLastFusedLocation() }
        if (fusedLast != null) {
            return@withContext Result.success(fusedLast.toCaptured())
        }

        // 4. Fallback to Android LocationManager providers
        val managerLast = getBestLocationManagerLastKnown()
        if (managerLast != null) {
            return@withContext Result.success(managerLast.toCaptured())
        }

        Result.failure(IllegalStateException("Unable to acquire GPS fix"))
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentFusedLocation(priority: Int): Location? =
        suspendCancellableCoroutine { cont ->
            val tokenSource = CancellationTokenSource()
            cont.invokeOnCancellation { tokenSource.cancel() }
            try {
                fusedClient.getCurrentLocation(priority, tokenSource.token)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun getLastFusedLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                fusedClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private fun getBestLocationManagerLastKnown(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (provider in providers) {
            val candidate = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            if (candidate != null) {
                if (best == null || candidate.time > best.time) {
                    best = candidate
                }
            }
        }
        return best
    }

    private fun Location.toCaptured(): CapturedLocation = CapturedLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else null
    )
}
