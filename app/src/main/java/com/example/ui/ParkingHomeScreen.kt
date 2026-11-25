package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ParkingSpotEntity
import com.example.data.preferences.FeeReminderState
import com.example.data.preferences.MapAppPreference
import com.example.navigation.InstalledMapApps
import com.example.notification.ParkingReminderScheduler
import com.example.ui.theme.DeepNavyCanvas
import com.example.ui.theme.DeepNavyOutline
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.DeepNavySurfaceHigh
import com.example.ui.theme.DeepNavySurfaceHighest
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MonoCoordTextStyle
import com.example.ui.theme.MonoTimerTextStyle
import com.example.ui.theme.OnSignalYellow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalCoral
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalYellow
import com.example.ui.theme.SignalYellowContainer
import java.io.File

@Composable
fun ParkingHomeScreen(
    uiState: ParkingUiState,
    snackbarHostState: SnackbarHostState,
    onSaveParkingClick: () -> Unit,
    onNavigateToCarClick: (ParkingSpotEntity) -> Unit,
    onTakePillarPhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onEditMemoClick: () -> Unit,
    onCopyCoordsClick: (ParkingSpotEntity) -> Unit,
    onShareLocationClick: (ParkingSpotEntity) -> Unit,
    onSelectMapApp: (MapAppPreference) -> Unit,
    onToggleFeeReminder: (Boolean) -> Unit,
    onPhotoPreviewClick: (String) -> Unit,
    onClearParkingClick: () -> Unit,
    onOpenHistoryClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .tacticalNavyGridBackground(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PureWhite,
                    contentColor = DeepNavyCanvas,
                    actionColor = DeepNavyCanvas,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TopHeaderBar(
                    isParked = uiState.activeSpot != null,
                    historyCount = uiState.historySpots.size,
                    onOpenHistoryClick = onOpenHistoryClick
                )

                AnimatedContent(
                    targetState = uiState.activeSpot,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "parking_state_transition"
                ) { activeSpot ->
                    if (activeSpot == null) {
                        EmptyParkingView(
                            onSaveParkingClick = onSaveParkingClick,
                            onTakePillarPhotoClick = onTakePillarPhotoClick
                        )
                    } else {
                        ActiveParkingView(
                            spot = activeSpot,
                            nowMillis = uiState.nowMillis,
                            preferredMapApp = uiState.preferredMapApp,
                            feeReminder = uiState.feeReminder,
                            installedMapApps = uiState.installedMapApps,
                            onNavigateToCarClick = { onNavigateToCarClick(activeSpot) },
                            onTakePillarPhotoClick = onTakePillarPhotoClick,
                            onPickFromGalleryClick = onPickFromGalleryClick,
                            onEditMemoClick = onEditMemoClick,
                            onCopyCoordsClick = { onCopyCoordsClick(activeSpot) },
                            onShareLocationClick = { onShareLocationClick(activeSpot) },
                            onSelectMapApp = onSelectMapApp,
                            onToggleFeeReminder = onToggleFeeReminder,
                            onPhotoPreviewClick = onPhotoPreviewClick,
                            onSaveNewLocationClick = onSaveParkingClick,
                            onClearParkingClick = onClearParkingClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeaderBar(
    isParked: Boolean,
    historyCount: Int,
    onOpenHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SignalYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = OnSignalYellow,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isParked) SignalGreen else SignalYellow)
                    )
                    Text(
                        text = if (isParked) {
                            stringResource(R.string.status_parked)
                        } else {
                            stringResource(R.string.status_empty)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isParked) SignalGreen else SignalYellow
                        )
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DeepNavySurface,
            border = BorderStroke(1.5.dp, DeepNavyOutline)
        ) {
            IconButton(
                onClick = onOpenHistoryClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("history_open_button")
            ) {
                BadgedBox(
                    badge = {
                        if (historyCount > 0) {
                            Badge(
                                containerColor = SignalYellow,
                                contentColor = OnSignalYellow
                            ) {
                                Text(
                                    text = historyCount.coerceAtMost(99).toString(),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = stringResource(R.string.cd_history),
                        tint = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyParkingView(
    onSaveParkingClick: () -> Unit,
    onTakePillarPhotoClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Contrast Hero Banner Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DeepNavySurface,
            border = BorderStroke(2.dp, DeepNavyOutline)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_parking_empty_banner_1795623931766),
                        contentDescription = stringResource(R.string.cd_empty_parking_banner),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DeepNavySurface.copy(alpha = 0.85f)
                                    ),
                                    startY = 140f
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.empty_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = stringResource(R.string.empty_description),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = IceWhite,
                            lineHeight = 24.sp
                        )
                    )
                }
            }
        }

        // Giant Signal Yellow Primary CTA: Save Current Parking Location
        HighContrastYellowButton(
            text = stringResource(R.string.action_save_parking),
            subtitle = "GPS 위도·경도 + 시각 + 층수/구역 메모",
            icon = Icons.Filled.MyLocation,
            onClick = onSaveParkingClick,
            modifier = Modifier.heightIn(min = 82.dp),
            testTag = "save_parking_primary_button"
        )

        // Large High-Contrast White CTA: Snap Pillar Photo Immediately
        HighContrastWhiteButton(
            text = stringResource(R.string.action_take_pillar_photo),
            icon = Icons.Filled.CameraAlt,
            onClick = onTakePillarPhotoClick,
            testTag = "take_pillar_photo_empty_button"
        )

        // Outdoor Readability Guide Strip
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = DeepNavySurfaceHigh,
            border = BorderStroke(1.5.dp, DeepNavyOutline)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = SignalYellow,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "저장 후에는 카카오맵 · 네이버 지도 · 구글 지도로 바로 도보 길찾기를 시작할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun ActiveParkingView(
    spot: ParkingSpotEntity,
    nowMillis: Long,
    preferredMapApp: MapAppPreference,
    feeReminder: FeeReminderState,
    installedMapApps: InstalledMapApps,
    onNavigateToCarClick: () -> Unit,
    onTakePillarPhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onEditMemoClick: () -> Unit,
    onCopyCoordsClick: () -> Unit,
    onShareLocationClick: () -> Unit,
    onSelectMapApp: (MapAppPreference) -> Unit,
    onToggleFeeReminder: (Boolean) -> Unit,
    onPhotoPreviewClick: (String) -> Unit,
    onSaveNewLocationClick: () -> Unit,
    onClearParkingClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Saved Parking Status Summary Card (Yellow High-Contrast Border)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_parking_card"),
            shape = RoundedCornerShape(24.dp),
            color = DeepNavySurface,
            border = BorderStroke(2.5.dp, SignalYellow)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Floor / Zone Memo Headline + Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditMemoClick() }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_floor_zone),
                            style = MaterialTheme.typography.labelMedium.copy(color = IceWhite)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spot.floorZoneMemo.ifBlank {
                                stringResource(R.string.label_no_memo)
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = if (spot.floorZoneMemo.isNotBlank()) SignalYellow else PureWhite.copy(alpha = 0.75f),
                                fontWeight = FontWeight.ExtraBold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("active_spot_memo_text")
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DeepNavySurfaceHighest,
                        border = BorderStroke(1.5.dp, PureWhite)
                    ) {
                        IconButton(
                            onClick = onEditMemoClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("edit_memo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.action_edit_memo),
                                tint = PureWhite
                            )
                        }
                    }
                }

                HorizontalDivider(color = DeepNavyOutline, thickness = 1.dp)

                // Elapsed Time & Saved Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = SignalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.label_elapsed_time),
                                style = MaterialTheme.typography.labelMedium.copy(color = IceWhite)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ParkingFormatters.formatElapsed(spot.savedAtMillis, nowMillis),
                            style = MonoTimerTextStyle.copy(color = SignalGreen),
                            modifier = Modifier.testTag("active_spot_elapsed_text")
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.label_saved_time),
                            style = MaterialTheme.typography.labelMedium.copy(color = IceWhite)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ParkingFormatters.formatSavedTime(spot.savedAtMillis),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.testTag("active_spot_time_text")
                        )
                    }
                }

                // GPS Coordinates Readout + Quick Copy/Share
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = DeepNavyCanvas,
                    border = BorderStroke(1.5.dp, DeepNavyOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.GpsFixed,
                                    contentDescription = null,
                                    tint = SignalYellow,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.label_gps_coords),
                                    style = MaterialTheme.typography.labelSmall.copy(color = IceWhite)
                                )
                                ParkingFormatters.formatAccuracy(spot.accuracyMeters)?.let { acc ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = acc,
                                        style = MaterialTheme.typography.labelSmall.copy(color = SignalGreen)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ParkingFormatters.formatCoordinates(spot.latitude, spot.longitude),
                                style = MonoCoordTextStyle.copy(color = PureWhite),
                                modifier = Modifier.testTag("active_spot_coords_text")
                            )
                        }
                        Row {
                            IconButton(
                                onClick = onCopyCoordsClick,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("copy_coords_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = stringResource(R.string.label_copy_coords),
                                    tint = PureWhite
                                )
                            }
                            IconButton(
                                onClick = onShareLocationClick,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("share_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = stringResource(R.string.label_share_location),
                                    tint = SignalYellow
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2-Hour Parking Fee Settlement Reminder Card
        ParkingFeeReminderStatusCard(
            spot = spot,
            feeReminder = feeReminder,
            nowMillis = nowMillis,
            onToggleFeeReminder = onToggleFeeReminder
        )

        // 2. Primary Walking Navigation Button (GIANT Signal Yellow)
        val mapSubtitle = when (preferredMapApp) {
            MapAppPreference.KAKAO -> "카카오맵 도보 길찾기 바로 실행"
            MapAppPreference.NAVER -> "네이버 지도 도보 길찾기 바로 실행"
            MapAppPreference.GOOGLE -> "구글 지도 도보 길찾기 바로 실행"
            MapAppPreference.AUTO -> when {
                installedMapApps.kakaoInstalled -> "설치된 카카오맵으로 도보 길찾기"
                installedMapApps.naverInstalled -> "설치된 네이버 지도로 도보 길찾기"
                installedMapApps.googleInstalled -> "설치된 구글 지도로 도보 길찾기"
                else -> "설치된 기본 지도 앱으로 도보 길찾기"
            }
        }
        HighContrastYellowButton(
            text = stringResource(R.string.action_navigate_to_car),
            subtitle = mapSubtitle,
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            onClick = onNavigateToCarClick,
            modifier = Modifier.heightIn(min = 80.dp),
            testTag = "navigate_to_car_button"
        )

        // Map App Quick Selector
        MapAppSelectorRow(
            selected = preferredMapApp,
            installedApps = installedMapApps,
            onSelect = onSelectMapApp
        )

        // 3. Pillar Photo Section + Large White Camera Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DeepNavySurface,
            border = BorderStroke(1.5.dp, DeepNavyOutline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.label_pillar_photo),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    if (!spot.photoPath.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SignalGreenContainer,
                            border = BorderStroke(1.dp, SignalGreen)
                        ) {
                            Text(
                                text = "사진 저장됨",
                                style = MaterialTheme.typography.labelSmall.copy(color = SignalGreen),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (!spot.photoPath.isNullOrBlank() && File(spot.photoPath).exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, PureWhite, RoundedCornerShape(16.dp))
                            .clickable { onPhotoPreviewClick(spot.photoPath) }
                            .testTag("saved_pillar_photo_card")
                    ) {
                        AsyncImage(
                            model = File(spot.photoPath),
                            contentDescription = stringResource(R.string.cd_pillar_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = DeepNavyCanvas.copy(alpha = 0.88f),
                            border = BorderStroke(1.5.dp, SignalYellow)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ZoomIn,
                                    contentDescription = null,
                                    tint = SignalYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.label_tap_to_zoom),
                                    style = MaterialTheme.typography.labelMedium.copy(color = PureWhite)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.label_no_photo_hint),
                        style = MaterialTheme.typography.bodyMedium.copy(color = IceWhite)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HighContrastWhiteButton(
                        text = if (spot.photoPath.isNullOrBlank()) {
                            stringResource(R.string.action_take_pillar_photo)
                        } else {
                            stringResource(R.string.action_retake_pillar_photo)
                        },
                        icon = Icons.Filled.CameraAlt,
                        onClick = onTakePillarPhotoClick,
                        modifier = Modifier.weight(1.45f),
                        testTag = "take_pillar_photo_button"
                    )
                    HighContrastOutlinedButton(
                        text = "앨범",
                        icon = Icons.Filled.PhotoLibrary,
                        onClick = onPickFromGalleryClick,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(64.dp),
                        testTag = "pick_gallery_button"
                    )
                }
            }
        }

        // 4. Bottom Secondary Controls: Save New Spot / Clear Parked Car
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HighContrastOutlinedButton(
                text = stringResource(R.string.action_save_new_parking),
                icon = Icons.Filled.MyLocation,
                onClick = onSaveNewLocationClick,
                borderColor = PureWhite,
                contentColor = PureWhite,
                modifier = Modifier.weight(1.1f),
                testTag = "save_new_parking_button"
            )
            HighContrastOutlinedButton(
                text = "출차 완료",
                icon = Icons.Filled.DeleteOutline,
                onClick = onClearParkingClick,
                borderColor = SignalCoral,
                contentColor = SignalCoral,
                modifier = Modifier.weight(0.9f),
                testTag = "clear_parking_button"
            )
        }
    }
}

@Composable
private fun ParkingFeeReminderStatusCard(
    spot: ParkingSpotEntity,
    feeReminder: FeeReminderState,
    nowMillis: Long,
    onToggleFeeReminder: (Boolean) -> Unit
) {
    val twoHourMark = spot.savedAtMillis + ParkingReminderScheduler.TWO_HOURS_MILLIS
    val isPastTwoHours = nowMillis >= twoHourMark
    val isReminderActiveForSpot = feeReminder.enabled && feeReminder.spotId == spot.id && !isPastTwoHours

    val containerColor = when {
        isPastTwoHours -> SignalYellowContainer
        isReminderActiveForSpot -> DeepNavySurfaceHigh
        else -> DeepNavySurface
    }
    val borderColor = when {
        isPastTwoHours -> SignalAmber
        isReminderActiveForSpot -> SignalYellow
        else -> DeepNavyOutline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_reminder_status_card"),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isReminderActiveForSpot || isPastTwoHours) {
                        Icons.Filled.NotificationsActive
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    contentDescription = null,
                    tint = when {
                        isPastTwoHours -> SignalAmber
                        isReminderActiveForSpot -> SignalYellow
                        else -> IceWhite
                    },
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.reminder_card_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val statusLine = when {
                        isPastTwoHours -> stringResource(R.string.reminder_status_passed)
                        isReminderActiveForSpot -> {
                            val triggerAt = feeReminder.triggerAtMillis.takeIf { it > 0L } ?: twoHourMark
                            "${ParkingFormatters.formatClockTime(triggerAt)} 알림 예정 · ${
                                ParkingFormatters.formatRemaining(triggerAt, nowMillis)
                            }"
                        }
                        else -> stringResource(R.string.reminder_status_off)
                    }
                    Text(
                        text = statusLine,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = when {
                                isPastTwoHours -> SignalAmber
                                isReminderActiveForSpot -> SignalYellow
                                else -> IceWhite
                            }
                        ),
                        modifier = Modifier.testTag("fee_reminder_status_text")
                    )
                }
            }

            if (!isPastTwoHours) {
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isReminderActiveForSpot,
                    onCheckedChange = onToggleFeeReminder,
                    modifier = Modifier.testTag("active_fee_reminder_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnSignalYellow,
                        checkedTrackColor = SignalYellow,
                        uncheckedThumbColor = PureWhite,
                        uncheckedTrackColor = DeepNavyCanvas,
                        uncheckedBorderColor = DeepNavyOutline
                    )
                )
            }
        }
    }
}
