package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ParkingSpotEntity
import com.example.ui.theme.DeepNavyCanvas
import com.example.ui.theme.DeepNavyOutline
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.DeepNavySurfaceHigh
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MonoCoordTextStyle
import com.example.ui.theme.OnSignalYellow
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SignalCoral
import com.example.ui.theme.SignalYellow
import java.io.File

@Composable
fun EditMemoDialog(
    memoText: String,
    onMemoChange: (String) -> Unit,
    onQuickChipClick: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavySurface,
        titleContentColor = PureWhite,
        textContentColor = PureWhite,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = stringResource(R.string.action_edit_memo),
                style = MaterialTheme.typography.titleLarge.copy(color = SignalYellow)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = memoText,
                    onValueChange = onMemoChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_memo_input"),
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
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalYellow,
                        unfocusedBorderColor = DeepNavyOutline,
                        focusedContainerColor = DeepNavyCanvas,
                        unfocusedContainerColor = DeepNavyCanvas,
                        cursorColor = SignalYellow
                    )
                )
                QuickPickChipRow(
                    items = QuickFloorPresets,
                    currentText = memoText,
                    onChipClick = onQuickChipClick
                )
                QuickPickChipRow(
                    items = QuickZonePresets,
                    currentText = memoText,
                    onChipClick = onQuickChipClick
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalYellow,
                    contentColor = OnSignalYellow
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("edit_memo_save_button")
            ) {
                Text("저장", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("edit_memo_cancel_button")
            ) {
                Text("취소", color = IceWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ClearParkingConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavySurface,
        titleContentColor = PureWhite,
        textContentColor = IceWhite,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = stringResource(R.string.confirm_clear_title),
                style = MaterialTheme.typography.titleLarge.copy(color = PureWhite)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.confirm_clear_message),
                style = MaterialTheme.typography.bodyLarge.copy(color = IceWhite)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalYellow,
                    contentColor = OnSignalYellow
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_clear_parking_button")
            ) {
                Text(
                    text = stringResource(R.string.confirm_clear_positive),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_parking_button")
            ) {
                Text("취소", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun PhotoPreviewDialog(
    photoPath: String,
    onRetake: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(onBack = onDismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = stringResource(R.string.cd_pillar_photo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (scale * zoom).coerceIn(1f, 4f)
                            scale = nextScale
                            offset = if (nextScale > 1f) {
                                offset + pan
                            } else {
                                Offset.Zero
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            // Top close bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DeepNavyCanvas.copy(alpha = 0.85f),
                    border = BorderStroke(1.5.dp, SignalYellow)
                ) {
                    Text(
                        text = "두 손가락으로 확대 가능",
                        style = MaterialTheme.typography.labelMedium.copy(color = SignalYellow),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = DeepNavyCanvas.copy(alpha = 0.9f),
                    border = BorderStroke(1.5.dp, PureWhite)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("photo_preview_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = PureWhite
                        )
                    }
                }
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HighContrastWhiteButton(
                    text = stringResource(R.string.action_retake_pillar_photo),
                    icon = Icons.Filled.CameraAlt,
                    onClick = {
                        onDismiss()
                        onRetake()
                    },
                    modifier = Modifier.weight(1.3f),
                    testTag = "photo_preview_retake_button"
                )
                HighContrastOutlinedButton(
                    text = "삭제",
                    icon = Icons.Filled.Delete,
                    onClick = onDelete,
                    borderColor = SignalCoral,
                    contentColor = SignalCoral,
                    modifier = Modifier
                        .weight(0.7f)
                        .height(64.dp),
                    testTag = "photo_preview_delete_button"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    historySpots: List<ParkingSpotEntity>,
    onNavigateSpot: (ParkingSpotEntity) -> Unit,
    onDeleteSpot: (Long) -> Unit,
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = SignalYellow,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineMedium.copy(color = PureWhite)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("history_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (historySpots.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DeepNavySurfaceHigh,
                    border = BorderStroke(1.5.dp, DeepNavyOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalParking,
                            contentDescription = null,
                            tint = SignalYellow,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.history_empty),
                            style = MaterialTheme.typography.bodyLarge.copy(color = IceWhite)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historySpots, key = { it.id }) { spot ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_item_${spot.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = DeepNavySurfaceHigh,
                            border = BorderStroke(1.5.dp, DeepNavyOutline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!spot.photoPath.isNullOrBlank()) {
                                    AsyncImage(
                                        model = File(spot.photoPath),
                                        contentDescription = stringResource(R.string.cd_pillar_photo),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.5.dp, SignalYellow, RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = spot.floorZoneMemo.ifBlank { "메모 없음" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = SignalYellow,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = ParkingFormatters.formatHistoryTime(spot.savedAtMillis),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ParkingFormatters.formatCoordinates(
                                            spot.latitude,
                                            spot.longitude
                                        ),
                                        style = MonoCoordTextStyle.copy(
                                            color = IceWhite,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigateSpot(spot) },
                                    modifier = Modifier.testTag("history_nav_${spot.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                        contentDescription = stringResource(R.string.history_restore),
                                        tint = SignalYellow
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSpot(spot.id) },
                                    modifier = Modifier.testTag("history_delete_${spot.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.history_delete),
                                        tint = SignalCoral
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
