package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ParkingSpotEntity
import com.example.navigation.MapLaunchResult
import com.example.navigation.MapNavigationLauncher
import com.example.notification.ParkingReminderScheduler
import com.example.ui.ClearParkingConfirmDialog
import com.example.ui.EditMemoDialog
import com.example.ui.HistorySheet
import com.example.ui.ParkingFormatters
import com.example.ui.ParkingHomeScreen
import com.example.ui.ParkingViewModel
import com.example.ui.PhotoCaptureTarget
import com.example.ui.PhotoPreviewDialog
import com.example.ui.SaveParkingSheet
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ParkingViewModel by viewModels {
        ParkingViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ParkingReminderScheduler.ensureNotificationChannel(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            MyApplicationTheme {
                MyCarSpotApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshInstalledMapApps(this)
    }
}

@Composable
private fun MyCarSpotApp(viewModel: ParkingViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var pendingCameraTarget by remember { mutableStateOf(PhotoCaptureTarget.ACTIVE_SPOT) }
    var pendingGalleryTarget by remember { mutableStateOf(PhotoCaptureTarget.ACTIVE_SPOT) }
    var pendingGpsRetryOnly by remember { mutableStateOf(false) }
    var pendingNotificationAction by remember { mutableStateOf<String?>(null) }

    // Collect ViewModel one-shot messages
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Real Camera Launcher (FileProvider URI -> app-private storage, zero broad storage permissions)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onCameraCaptureResult(
            success = success,
            hasLocationPermissionForNewSave = viewModel.hasLocationPermission()
        )
    }

    fun launchCameraNow(target: PhotoCaptureTarget) {
        runCatching {
            val pending = viewModel.prepareCameraCapture(target)
            cameraLauncher.launch(pending.contentUri)
        }.onFailure {
            viewModel.postMessage("카메라를 실행할 수 없습니다. '앨범' 버튼을 이용해 주세요.")
        }
    }

    // Camera Runtime Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraNow(pendingCameraTarget)
        } else {
            coroutineScope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = "주차 기둥 사진을 촬영하려면 카메라 권한이 필요합니다.",
                    actionLabel = "설정 열기",
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    openAppSettings(context)
                }
            }
        }
    }

    fun requestCameraFor(target: PhotoCaptureTarget) {
        pendingCameraTarget = target
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCameraNow(target)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Zero-Permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onGalleryImageSelected(
            uri = uri,
            target = pendingGalleryTarget,
            hasLocationPermissionForNewSave = viewModel.hasLocationPermission()
        )
    }

    fun launchGalleryFor(target: PhotoCaptureTarget) {
        pendingGalleryTarget = target
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Location Runtime Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (pendingGpsRetryOnly) {
                viewModel.retryGpsCapture()
            } else {
                viewModel.openSaveSheetAndFetchGps()
            }
        } else {
            coroutineScope.launch {
                val res = snackbarHostState.showSnackbar(
                    message = "현재 GPS 좌표를 저장하려면 위치 권한이 필요합니다.",
                    actionLabel = "설정 열기",
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    openAppSettings(context)
                }
            }
        }
        pendingGpsRetryOnly = false
    }

    fun requestLocationAndSave(retryOnly: Boolean) {
        pendingGpsRetryOnly = retryOnly
        if (viewModel.hasLocationPermission()) {
            if (retryOnly) {
                viewModel.retryGpsCapture()
            } else {
                viewModel.openSaveSheetAndFetchGps()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Notification Runtime Permission Launcher (Android 13+ / API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingNotificationAction
        pendingNotificationAction = null
        when (action) {
            "save_spot" -> {
                viewModel.confirmSaveParkingSpot(notificationPermissionGranted = granted)
                if (!granted) {
                    coroutineScope.launch {
                        val res = snackbarHostState.showSnackbar(
                            message = "2시간 뒤 주차 요금 정산 알림을 받으려면 알림 권한이 필요합니다.",
                            actionLabel = "설정 열기",
                            duration = SnackbarDuration.Long
                        )
                        if (res == SnackbarResult.ActionPerformed) {
                            openAppSettings(context)
                        }
                    }
                }
            }
            "toggle_active_reminder" -> {
                if (granted) {
                    viewModel.toggleActiveSpotFeeReminder(enabled = true)
                } else {
                    coroutineScope.launch {
                        val res = snackbarHostState.showSnackbar(
                            message = "2시간 뒤 주차 요금 정산 알림을 받으려면 알림 권한이 필요합니다.",
                            actionLabel = "설정 열기",
                            duration = SnackbarDuration.Long
                        )
                        if (res == SnackbarResult.ActionPerformed) {
                            openAppSettings(context)
                        }
                    }
                }
            }
        }
    }

    fun confirmSaveWithOptionalNotificationPermission() {
        if (!uiState.draftFeeReminderEnabled) {
            viewModel.confirmSaveParkingSpot(notificationPermissionGranted = false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationAction = "save_spot"
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.confirmSaveParkingSpot(
                notificationPermissionGranted = ParkingReminderScheduler.hasNotificationPermission(context)
            )
        }
    }

    fun handleActiveReminderToggle(enabled: Boolean) {
        if (!enabled) {
            viewModel.toggleActiveSpotFeeReminder(enabled = false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingNotificationAction = "toggle_active_reminder"
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.toggleActiveSpotFeeReminder(enabled = true)
        }
    }

    fun navigateToSpot(spot: ParkingSpotEntity) {
        val result = MapNavigationLauncher.launchWalkingDirections(
            context = context,
            latitude = spot.latitude,
            longitude = spot.longitude,
            destinationLabel = spot.floorZoneMemo.ifBlank { "내 차 주차 위치" },
            preference = uiState.preferredMapApp
        )
        when (result) {
            is MapLaunchResult.Launched -> {
                viewModel.postMessage("${result.appLabel} 도보 길찾기를 실행합니다.")
            }
            MapLaunchResult.NoAppAvailable -> {
                viewModel.postMessage("실행할 수 있는 지도 앱을 찾지 못했습니다.")
            }
        }
    }

    ParkingHomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSaveParkingClick = { requestLocationAndSave(retryOnly = false) },
        onNavigateToCarClick = ::navigateToSpot,
        onTakePillarPhotoClick = { requestCameraFor(PhotoCaptureTarget.ACTIVE_SPOT) },
        onPickFromGalleryClick = { launchGalleryFor(PhotoCaptureTarget.ACTIVE_SPOT) },
        onEditMemoClick = viewModel::openEditMemoDialog,
        onCopyCoordsClick = { spot ->
            copyCoordinatesToClipboard(context, spot)
            viewModel.postMessage("GPS 좌표가 클립보드에 복사되었습니다.")
        },
        onShareLocationClick = { spot ->
            shareParkingSpot(context, spot)
        },
        onSelectMapApp = viewModel::selectMapApp,
        onToggleFeeReminder = ::handleActiveReminderToggle,
        onPhotoPreviewClick = viewModel::openPhotoPreview,
        onClearParkingClick = viewModel::openClearConfirmDialog,
        onOpenHistoryClick = viewModel::openHistorySheet
    )

    if (uiState.isSaveSheetOpen) {
        SaveParkingSheet(
            gpsState = uiState.gpsState,
            draftMemo = uiState.draftMemo,
            draftPhotoPath = uiState.draftPhotoPath,
            feeReminderEnabled = uiState.draftFeeReminderEnabled,
            onMemoChange = viewModel::updateDraftMemo,
            onQuickChipClick = viewModel::appendQuickChipToDraft,
            onFeeReminderToggle = viewModel::setDraftFeeReminderEnabled,
            onRetryGps = { requestLocationAndSave(retryOnly = true) },
            onTakePhotoClick = { requestCameraFor(PhotoCaptureTarget.SAVE_SHEET_DRAFT) },
            onPickGalleryClick = { launchGalleryFor(PhotoCaptureTarget.SAVE_SHEET_DRAFT) },
            onRemoveDraftPhoto = viewModel::removeDraftPhoto,
            onConfirmSave = ::confirmSaveWithOptionalNotificationPermission,
            onDismiss = viewModel::dismissSaveSheet
        )
    }

    if (uiState.isEditMemoDialogOpen) {
        EditMemoDialog(
            memoText = uiState.editMemoText,
            onMemoChange = viewModel::updateEditMemoText,
            onQuickChipClick = viewModel::appendQuickChipToEditMemo,
            onConfirm = viewModel::saveEditedMemo,
            onDismiss = viewModel::dismissEditMemoDialog
        )
    }

    if (uiState.isClearConfirmOpen) {
        ClearParkingConfirmDialog(
            onConfirm = viewModel::confirmClearActiveParking,
            onDismiss = viewModel::dismissClearConfirmDialog
        )
    }

    uiState.previewPhotoPath?.let { photoPath ->
        PhotoPreviewDialog(
            photoPath = photoPath,
            onRetake = { requestCameraFor(PhotoCaptureTarget.ACTIVE_SPOT) },
            onDelete = viewModel::deleteActiveSpotPhoto,
            onDismiss = viewModel::dismissPhotoPreview
        )
    }

    if (uiState.isHistorySheetOpen) {
        HistorySheet(
            historySpots = uiState.historySpots,
            onNavigateSpot = { spot ->
                viewModel.dismissHistorySheet()
                navigateToSpot(spot)
            },
            onDeleteSpot = viewModel::deleteHistorySpot,
            onDismiss = viewModel::dismissHistorySheet
        )
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun copyCoordinatesToClipboard(context: Context, spot: ParkingSpotEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val text = ParkingFormatters.formatCoordinates(spot.latitude, spot.longitude)
    clipboard.setPrimaryClip(ClipData.newPlainText("주차 GPS 좌표", text))
}

private fun shareParkingSpot(context: Context, spot: ParkingSpotEntity) {
    val coords = ParkingFormatters.formatCoordinates(spot.latitude, spot.longitude)
    val memoLine = if (spot.floorZoneMemo.isNotBlank()) "층수/구역: ${spot.floorZoneMemo}\n" else ""
    val timeLine = "저장 시각: ${ParkingFormatters.formatSavedTime(spot.savedAtMillis)}\n"
    val mapLink = "https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}&travelmode=walking"
    val body = "[내 차 주차 위치]\n$memoLine$timeLine" + "GPS 좌표: $coords\n도보 길찾기: $mapLink"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, body)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, "주차 위치 공유"))
    }
}
