package com.example.navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.data.preferences.MapAppPreference
import java.net.URLEncoder
import java.util.Locale

data class InstalledMapApps(
    val kakaoInstalled: Boolean,
    val naverInstalled: Boolean,
    val googleInstalled: Boolean
)

sealed interface MapLaunchResult {
    data class Launched(val appLabel: String) : MapLaunchResult
    data object NoAppAvailable : MapLaunchResult
}

object MapNavigationLauncher {
    private const val PKG_KAKAO = "net.daum.android.map"
    private const val PKG_NAVER = "com.nhn.android.nmap"
    private const val PKG_GOOGLE = "com.google.android.apps.maps"

    fun detectInstalledApps(context: Context): InstalledMapApps {
        val pm = context.packageManager
        return InstalledMapApps(
            kakaoInstalled = isPackageInstalled(pm, PKG_KAKAO),
            naverInstalled = isPackageInstalled(pm, PKG_NAVER),
            googleInstalled = isPackageInstalled(pm, PKG_GOOGLE)
        )
    }

    fun launchWalkingDirections(
        context: Context,
        latitude: Double,
        longitude: Double,
        destinationLabel: String,
        preference: MapAppPreference
    ): MapLaunchResult {
        val label = destinationLabel.ifBlank { "내 차 주차 위치" }
        val orderedAttempts = when (preference) {
            MapAppPreference.KAKAO -> listOf(
                ::tryKakaoMap,
                ::tryKakaoWeb,
                ::tryNaverMap,
                ::tryGoogleMapsWalking,
                ::tryGeoUniversal,
                ::tryGoogleMapsWeb
            )
            MapAppPreference.NAVER -> listOf(
                ::tryNaverMap,
                ::tryKakaoMap,
                ::tryGoogleMapsWalking,
                ::tryGeoUniversal,
                ::tryGoogleMapsWeb
            )
            MapAppPreference.GOOGLE -> listOf(
                ::tryGoogleMapsWalking,
                ::tryGeoUniversal,
                ::tryGoogleMapsWeb,
                ::tryKakaoMap,
                ::tryNaverMap
            )
            MapAppPreference.AUTO -> listOf(
                ::tryKakaoMap,
                ::tryNaverMap,
                ::tryGoogleMapsWalking,
                ::tryGeoUniversal,
                ::tryGoogleMapsWeb
            )
        }

        for (attempt in orderedAttempts) {
            val launchedLabel = attempt(context, latitude, longitude, label)
            if (launchedLabel != null) {
                return MapLaunchResult.Launched(launchedLabel)
            }
        }
        return MapLaunchResult.NoAppAvailable
    }

    private fun tryKakaoMap(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        if (!isPackageInstalled(context.packageManager, PKG_KAKAO)) return null
        val uri = Uri.parse(
            String.format(Locale.US, "kakaomap://route?ep=%.6f,%.6f&by=FOOT", lat, lng)
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(PKG_KAKAO)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "카카오맵" else null
    }

    private fun tryKakaoWeb(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        val encoded = URLEncoder.encode(label, "UTF-8")
        val uri = Uri.parse(
            String.format(Locale.US, "https://map.kakao.com/link/to/%s,%.6f,%.6f", encoded, lat, lng)
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "카카오맵(웹)" else null
    }

    private fun tryNaverMap(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        if (!isPackageInstalled(context.packageManager, PKG_NAVER)) return null
        val encodedName = URLEncoder.encode(label, "UTF-8")
        val appName = URLEncoder.encode(context.packageName, "UTF-8")
        val uri = Uri.parse(
            String.format(
                Locale.US,
                "nmap://route/walk?dlat=%.6f&dlng=%.6f&dname=%s&appname=%s",
                lat,
                lng,
                encodedName,
                appName
            )
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(PKG_NAVER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "네이버 지도" else null
    }

    private fun tryGoogleMapsWalking(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        if (!isPackageInstalled(context.packageManager, PKG_GOOGLE)) return null
        val uri = Uri.parse(
            String.format(Locale.US, "google.navigation:q=%.6f,%.6f&mode=w", lat, lng)
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(PKG_GOOGLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "구글 지도" else null
    }

    private fun tryGeoUniversal(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        val encodedLabel = Uri.encode(label)
        val uri = Uri.parse(
            String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f(%s)", lat, lng, lat, lng, encodedLabel)
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "기본 지도 앱" else null
    }

    private fun tryGoogleMapsWeb(
        context: Context,
        lat: Double,
        lng: Double,
        label: String
    ): String? {
        val uri = Uri.parse(
            String.format(
                Locale.US,
                "https://www.google.com/maps/dir/?api=1&destination=%.6f,%.6f&travelmode=walking",
                lat,
                lng
            )
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (startIfPossible(context, intent)) "구글 지도(웹)" else null
    }

    private fun startIfPossible(context: Context, intent: Intent): Boolean {
        return runCatching {
            val resolved = intent.resolveActivity(context.packageManager)
            if (resolved != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return runCatching {
            pm.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
    }
}
