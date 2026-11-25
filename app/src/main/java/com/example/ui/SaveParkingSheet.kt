package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.notification.ParkingReminderScheduler
import com.example.ui.theme.DeepNavyCanvas
import com.example.ui.theme.DeepNavyOutline
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.DeepNavySurfaceHigh
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MonoCoordTextStyle
import com.example.ui.theme.OnSignalYellow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalCoral
import com.example.ui.theme.SignalCoralContainer
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenContainer
import com.example.ui.theme.SignalYellow
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveParkingSheet(
    gpsState: GpsCaptureState,
    draftMemo: String,
    draftPhotoPath: String?,
    feeReminderEnabled: Boolean,
    onMemoChange: (String) -> Unit,
    onQuickChipClick: (String) -> Unit,
    onFeeReminderToggle: (Boolean) -> Unit,
    onRetryGps: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickGalleryClick: () -> Unit,
    onRemoveDraftPhoto: () -> Unit,
    onConfirmSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavySurface,
        contentColor = PureWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 48.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(SignalYellow)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        tint = SignalYellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.save_sheet_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("save_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = PureWhite
                    )
                }
            }

            // GPS Status Card
            GpsCaptureStatusCard(
                gpsState = gpsState,
                onRetryGps = onRetryGps
            )

            // Floor / Zone Memo Input
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.save_sheet_memo_label),
                    style = MaterialTheme.typography.titleMedium.copy(color = SignalYellow)
                )
                OutlinedTextField(
                    value = draftMemo,
                    onValueChange = onMemoChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_memo_input"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.save_sheet_memo_placeholder),
                            color = IceWhite.copy(alpha = 0.65f)
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalYellow,
                        unfocusedBorderColor = DeepNavyOutline,
                        focusedContainerColor = DeepNavyCanvas,
                        unfocusedContainerColor = DeepNavyCanvas,
                        cursorColor = SignalYellow
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (gpsState is GpsCaptureState.Captured) {
                                onConfirmSave()
                            }
                        }
                    )
                )

                Text(
                    text = stringResource(R.string.save_sheet_quick_floor),
                    style = MaterialTheme.typography.labelMedium.copy(color = IceWhite)
                )
                QuickPickChipRow(
                    items = QuickFloorPresets,
                    currentText = draftMemo,
                    onChipClick = onQuickChipClick
                )

                Text(
                    text = stringResource(R.string.save_sheet_quick_zone),
                    style = MaterialTheme.typography.labelMedium.copy(color = IceWhite)
                )
                QuickPickChipRow(
                    items = QuickZonePresets,
                    currentText = draftMemo,
                    onChipClick = onQuickChipClick
                )
            }

            // Optional Pillar Photo Attachment
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.label_pillar_photo),
                    style = MaterialTheme.typography.titleMedium.copy(color = PureWhite)
                )
                if (draftPhotoPath != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, SignalYellow, RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = File(draftPhotoPath),
                            contentDescription = stringResource(R.string.cd_pillar_photo),
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp),
                            shape = CircleShape,
                            color = DeepNavyCanvas.copy(alpha = 0.88f),
                            border = BorderStroke(1.5.dp, PureWhite)
                        ) {
                            IconButton(
                                onClick = onRemoveDraftPhoto,
                                modifier = Modifier.testTag("draft_photo_remove_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.label_delete_photo),
                                    tint = SignalCoral
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HighContrastWhiteButton(
                        text = if (draftPhotoPath == null) {
                            stringResource(R.string.action_take_pillar_photo)
                        } else {
                            stringResource(R.string.action_retake_pillar_photo)
                        },
                        icon = Icons.Filled.CameraAlt,
                        onClick = onTakePhotoClick,
                        modifier = Modifier.weight(1.4f),
                        testTag = "sheet_take_photo_button"
                    )
                    HighContrastOutlinedButton(
                        text = "앨범",
                        icon = Icons.Filled.PhotoLibrary,
                        onClick = onPickGalleryClick,
                        modifier = Modifier
                            .weight(0.75f)
                            .height(64.dp),
                        testTag = "sheet_pick_gallery_button"
                    )
                }
            }

            // 2-Hour Parking Fee Reminder Switch Card
            val previewTriggerTime = (gpsState as? GpsCaptureState.Captured)?.let {
                ParkingFormatters.formatClockTime(
                    it.capturedAtMillis + ParkingReminderScheduler.TWO_HOURS_MILLIS
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DeepNavySurfaceHigh,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (feeReminderEnabled) SignalYellow else DeepNavyOutline
                )
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
                            imageVector = if (feeReminderEnabled) {
                                Icons.Filled.NotificationsActive
                            } else {
                                Icons.Filled.NotificationsOff
                            },
                            contentDescription = null,
                            tint = if (feeReminderEnabled) SignalYellow else IceWhite,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.reminder_card_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = if (feeReminderEnabled && previewTriggerTime != null) {
                                    "${previewTriggerTime}에 요금 정산 알림 예약"
                                } else {
                                    stringResource(R.string.reminder_sheet_subtitle)
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (feeReminderEnabled) SignalYellow else IceWhite
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = feeReminderEnabled,
                        onCheckedChange = onFeeReminderToggle,
                        modifier = Modifier.testTag("save_sheet_reminder_switch"),
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

            Spacer(modifier = Modifier.height(2.dp))

            // Confirm Save Button
            HighContrastYellowButton(
                text = stringResource(R.string.save_sheet_confirm),
                icon = Icons.Filled.CheckCircle,
                enabled = gpsState is GpsCaptureState.Captured,
                onClick = onConfirmSave,
                subtitle = if (gpsState is GpsCaptureState.Captured) {
                    ParkingFormatters.formatSavedTime(gpsState.capturedAtMillis)
                } else {
                    stringResource(R.string.save_sheet_gps_loading)
                },
                testTag = "confirm_save_parking_button"
            )
        }
    }
}

@Composable
private fun GpsCaptureStatusCard(
    gpsState: GpsCaptureState,
    onRetryGps: () -> Unit
) {
    val borderColor = when (gpsState) {
        is GpsCaptureState.Captured -> SignalGreen
        is GpsCaptureState.Error -> SignalCoral
        else -> SignalYellow
    }
    val containerColor = when (gpsState) {
        is GpsCaptureState.Captured -> SignalGreenContainer
        is GpsCaptureState.Error -> SignalCoralContainer
        else -> DeepNavySurfaceHigh
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gps_status_card"),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        when (gpsState) {
            is GpsCaptureState.Idle,
            is GpsCaptureState.Acquiring -> {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = SignalYellow,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.save_sheet_gps_loading),
                            style = MaterialTheme.typography.titleMedium.copy(color = SignalYellow)
                        )
                        Text(
                            text = "현재 위도·경도 좌표를 정확하게 측정하고 있습니다.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = IceWhite)
                        )
                    }
                }
            }

            is GpsCaptureState.Captured -> {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.GpsFixed,
                                contentDescription = null,
                                tint = SignalGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.save_sheet_gps_ready),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = SignalGreen,
                                    fontSize = 15.sp
                                )
                            )
                            ParkingFormatters.formatAccuracy(gpsState.location.accuracyMeters)?.let { acc ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = acc,
                                    style = MaterialTheme.typography.labelSmall.copy(color = PureWhite)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ParkingFormatters.formatCoordinates(
                                gpsState.location.latitude,
                                gpsState.location.longitude
                            ),
                            style = MonoCoordTextStyle.copy(color = PureWhite)
                        )
                    }
                    IconButton(
                        onClick = onRetryGps,
                        modifier = Modifier.testTag("gps_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.save_sheet_gps_retry),
                            tint = PureWhite
                        )
                    }
                }
            }

            is GpsCaptureState.Error -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = SignalCoral,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = gpsState.reason,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    HighContrastOutlinedButton(
                        text = stringResource(R.string.save_sheet_gps_retry),
                        icon = Icons.Filled.Refresh,
                        onClick = onRetryGps,
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SignalYellow,
                        contentColor = SignalYellow,
                        testTag = "gps_retry_button"
                    )
                }
            }
        }
    }
}
