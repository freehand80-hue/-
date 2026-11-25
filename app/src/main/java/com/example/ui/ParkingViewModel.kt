package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ParkingDatabase
import com.example.data.local.ParkingSpotEntity
import com.example.data.preferences.FeeReminderState
import com.example.data.preferences.MapAppPreference
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ParkingRepository
import com.example.location.CapturedLocation
import com.example.location.LocationFetcher
import com.example.media.PendingPhotoCapture
import com.example.media.PhotoStorageManager
import com.example.navigation.InstalledMapApps
import com.example.navigation.MapNavigationLauncher
import com.example.notification.ParkingReminderScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface GpsCaptureState {
    data object Idle : GpsCaptureState
    data object Acquiring : GpsCaptureState
    data class Captured(
        val location: CapturedLocation,
        val capturedAtMillis: Long
    ) : GpsCaptureState
    data class Error(val reason: String) : GpsCaptureState
}

enum class PhotoCaptureTarget {
    ACTIVE_SPOT,
    SAVE_SHEET_DRAFT
}

data class ParkingUiState(
    val activeSpot: ParkingSpotEntity? = null,
    val historySpots: List<ParkingSpotEntity> = emptyList(),
    val preferredMapApp: MapAppPreference = MapAppPreference.AUTO,
    val feeReminder: FeeReminderState = FeeReminderState(),
    val installedMapApps: InstalledMapApps = InstalledMapApps(false, false, false),
    val nowMillis: Long = System.currentTimeMillis(),
    val isSaveSheetOpen: Boolean = false,
    val gpsState: GpsCaptureState = GpsCaptureState.Idle,
    val draftMemo: String = "",
    val draftPhotoPath: String? = null,
    val draftFeeReminderEnabled: Boolean = true,
    val isEditMemoDialogOpen: Boolean = false,
    val editMemoText: String = "",
    val isHistorySheetOpen: Boolean = false,
    val previewPhotoPath: String? = null,
    val isClearConfirmOpen: Boolean = false
)

private data class LocalModalState(
    val nowMillis: Long = System.currentTimeMillis(),
    val isSaveSheetOpen: Boolean = false,
    val gpsState: GpsCaptureState = GpsCaptureState.Idle,
    val draftMemo: String = "",
    val draftPhotoPath: String? = null,
    val draftFeeReminderEnabled: Boolean = true,
    val isEditMemoDialogOpen: Boolean = false,
    val editMemoText: String = "",
    val isHistorySheetOpen: Boolean = false,
    val previewPhotoPath: String? = null,
    val isClearConfirmOpen: Boolean = false
)

class ParkingViewModel(
    private val appContext: Context,
    private val repository: ParkingRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationFetcher: LocationFetcher,
    private val photoStorageManager: PhotoStorageManager,
    initialInstalledMapApps: InstalledMapApps
) : ViewModel() {

    private val localState = MutableStateFlow(LocalModalState())
    private val installedMapsFlow = MutableStateFlow(initialInstalledMapApps)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var pendingCaptureFilePath: String? = null
    private var pendingCaptureTarget: PhotoCaptureTarget = PhotoCaptureTarget.ACTIVE_SPOT

    private val persistedPrefsFlow = combine(
        preferencesRepository.preferredMapApp,
        preferencesRepository.feeReminderState
    ) { mapPref, reminder -> mapPref to reminder }

    val uiState: StateFlow<ParkingUiState> = combine(
        repository.activeSpot,
        repository.historySpots,
        persistedPrefsFlow,
        installedMapsFlow,
        localState
    ) { activeSpot, historySpots, prefsPair, installedMaps, modal ->
        val (preferredMap, feeReminder) = prefsPair
        ParkingUiState(
            activeSpot = activeSpot,
            historySpots = historySpots,
            preferredMapApp = preferredMap,
            feeReminder = feeReminder,
            installedMapApps = installedMaps,
            nowMillis = modal.nowMillis,
            isSaveSheetOpen = modal.isSaveSheetOpen,
            gpsState = modal.gpsState,
            draftMemo = modal.draftMemo,
            draftPhotoPath = modal.draftPhotoPath,
            draftFeeReminderEnabled = modal.draftFeeReminderEnabled,
            isEditMemoDialogOpen = modal.isEditMemoDialogOpen,
            editMemoText = modal.editMemoText,
            isHistorySheetOpen = modal.isHistorySheetOpen,
            previewPhotoPath = modal.previewPhotoPath,
            isClearConfirmOpen = modal.isClearConfirmOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ParkingUiState(installedMapApps = initialInstalledMapApps)
    )

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
                localState.update { it.copy(nowMillis = System.currentTimeMillis()) }
            }
        }
    }

    fun refreshInstalledMapApps(context: Context) {
        installedMapsFlow.value = MapNavigationLauncher.detectInstalledApps(context)
    }

    fun hasLocationPermission(): Boolean = locationFetcher.hasLocationPermission()

    fun openSaveSheetAndFetchGps(initialPhotoPath: String? = null) {
        localState.update {
            it.copy(
                isSaveSheetOpen = true,
                gpsState = GpsCaptureState.Acquiring,
                draftMemo = "",
                draftPhotoPath = initialPhotoPath,
                draftFeeReminderEnabled = true
            )
        }
        acquireGpsForSaveSheet()
    }

    fun setDraftFeeReminderEnabled(enabled: Boolean) {
        localState.update { it.copy(draftFeeReminderEnabled = enabled) }
    }

    fun retryGpsCapture() {
        localState.update { it.copy(gpsState = GpsCaptureState.Acquiring) }
        acquireGpsForSaveSheet()
    }

    private fun acquireGpsForSaveSheet() {
        viewModelScope.launch {
            val result = locationFetcher.fetchCurrentLocation()
            result.onSuccess { location ->
                localState.update {
                    it.copy(
                        gpsState = GpsCaptureState.Captured(
                            location = location,
                            capturedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }.onFailure {
                localState.update {
                    it.copy(
                        gpsState = GpsCaptureState.Error(
                            "현재 GPS 좌표를 가져오지 못했습니다. 위치 서비스(GPS)가 켜져 있는지 확인한 뒤 다시 시도해 주세요."
                        )
                    )
                }
            }
        }
    }

    fun updateDraftMemo(memo: String) {
        localState.update { it.copy(draftMemo = memo) }
    }

    fun appendQuickChipToDraft(chip: String) {
        localState.update { state ->
            val current = state.draftMemo.trim()
            val updated = when {
                current.isEmpty() -> chip
                current.contains(chip) -> current
                else -> "$current $chip"
            }
            state.copy(draftMemo = updated)
        }
    }

    fun dismissSaveSheet() {
        val unusedDraftPhoto = localState.value.draftPhotoPath
        localState.update {
            it.copy(
                isSaveSheetOpen = false,
                gpsState = GpsCaptureState.Idle,
                draftMemo = "",
                draftPhotoPath = null
            )
        }
        if (unusedDraftPhoto != null) {
            photoStorageManager.deletePhotoFile(unusedDraftPhoto)
        }
    }

    fun confirmSaveParkingSpot(notificationPermissionGranted: Boolean) {
        val state = localState.value
        val captured = state.gpsState as? GpsCaptureState.Captured ?: return
        viewModelScope.launch {
            ParkingReminderScheduler.cancelReminder(appContext)
            val newSpotId = repository.saveNewParkingSpot(
                latitude = captured.location.latitude,
                longitude = captured.location.longitude,
                accuracyMeters = captured.location.accuracyMeters,
                savedAtMillis = captured.capturedAtMillis,
                floorZoneMemo = state.draftMemo,
                photoPath = state.draftPhotoPath
            )

            val reminderMessage = if (state.draftFeeReminderEnabled && notificationPermissionGranted) {
                val triggerAt = ParkingReminderScheduler.scheduleTwoHourReminder(
                    context = appContext,
                    spotId = newSpotId,
                    memo = state.draftMemo.trim(),
                    savedAtMillis = captured.capturedAtMillis
                )
                preferencesRepository.setFeeReminder(
                    enabled = true,
                    spotId = newSpotId,
                    triggerAtMillis = triggerAt
                )
                "주차 위치 저장 완료! ${ParkingFormatters.formatClockTime(triggerAt)}에 2시간 정산 알림이 울립니다."
            } else {
                preferencesRepository.clearFeeReminder()
                if (state.draftFeeReminderEnabled && !notificationPermissionGranted) {
                    "주차 위치는 저장되었지만 알림 권한이 없어 2시간 정산 알림은 꺼졌습니다."
                } else {
                    "주차 위치가 저장되었습니다."
                }
            }

            localState.update {
                it.copy(
                    isSaveSheetOpen = false,
                    gpsState = GpsCaptureState.Idle,
                    draftMemo = "",
                    draftPhotoPath = null,
                    nowMillis = System.currentTimeMillis()
                )
            }
            _messages.tryEmit(reminderMessage)
        }
    }

    fun toggleActiveSpotFeeReminder(enabled: Boolean) {
        val active = uiState.value.activeSpot ?: return
        viewModelScope.launch {
            if (!enabled) {
                ParkingReminderScheduler.cancelReminder(appContext)
                preferencesRepository.clearFeeReminder()
                _messages.tryEmit("2시간 주차 요금 정산 알림이 해제되었습니다.")
                return@launch
            }

            val twoHourTarget = active.savedAtMillis + ParkingReminderScheduler.TWO_HOURS_MILLIS
            val now = System.currentTimeMillis()
            if (twoHourTarget <= now) {
                _messages.tryEmit("이미 주차 후 2시간이 경과했습니다. 주차 요금을 확인해 주세요.")
                return@launch
            }

            val triggerAt = ParkingReminderScheduler.scheduleTwoHourReminder(
                context = appContext,
                spotId = active.id,
                memo = active.floorZoneMemo,
                savedAtMillis = active.savedAtMillis,
                triggerAtMillis = twoHourTarget
            )
            preferencesRepository.setFeeReminder(
                enabled = true,
                spotId = active.id,
                triggerAtMillis = triggerAt
            )
            _messages.tryEmit("${ParkingFormatters.formatClockTime(triggerAt)}에 2시간 정산 알림이 울립니다.")
        }
    }

    fun prepareCameraCapture(target: PhotoCaptureTarget): PendingPhotoCapture {
        val destination = photoStorageManager.createCaptureDestination()
        pendingCaptureFilePath = destination.filePath
        pendingCaptureTarget = target
        return destination
    }

    fun onCameraCaptureResult(success: Boolean, hasLocationPermissionForNewSave: Boolean) {
        val capturedPath = pendingCaptureFilePath
        val target = pendingCaptureTarget
        pendingCaptureFilePath = null

        if (!success || capturedPath == null) {
            photoStorageManager.deletePhotoFile(capturedPath)
            return
        }

        handleSavedPhotoPath(capturedPath, target, hasLocationPermissionForNewSave)
    }

    fun onGalleryImageSelected(
        uri: Uri?,
        target: PhotoCaptureTarget,
        hasLocationPermissionForNewSave: Boolean
    ) {
        if (uri == null) return
        viewModelScope.launch {
            val savedPath = photoStorageManager.copyFromUri(uri)
            if (savedPath == null) {
                _messages.tryEmit("사진을 불러오지 못했습니다.")
                return@launch
            }
            handleSavedPhotoPath(savedPath, target, hasLocationPermissionForNewSave)
        }
    }

    private fun handleSavedPhotoPath(
        newPath: String,
        target: PhotoCaptureTarget,
        hasLocationPermissionForNewSave: Boolean
    ) {
        when (target) {
            PhotoCaptureTarget.SAVE_SHEET_DRAFT -> {
                val oldDraft = localState.value.draftPhotoPath
                localState.update { it.copy(draftPhotoPath = newPath) }
                if (oldDraft != null && oldDraft != newPath) {
                    photoStorageManager.deletePhotoFile(oldDraft)
                }
                _messages.tryEmit("기둥 사진이 첨부되었습니다.")
            }

            PhotoCaptureTarget.ACTIVE_SPOT -> {
                val active = uiState.value.activeSpot
                if (active != null) {
                    viewModelScope.launch {
                        val oldPhoto = active.photoPath
                        repository.updatePhotoPath(active.id, newPath)
                        if (oldPhoto != null && oldPhoto != newPath) {
                            photoStorageManager.deletePhotoFile(oldPhoto)
                        }
                        _messages.tryEmit("기둥 사진이 저장되었습니다.")
                    }
                } else {
                    // User snapped a pillar photo first before saving GPS — open save sheet with photo ready!
                    localState.update {
                        it.copy(
                            isSaveSheetOpen = true,
                            draftMemo = "",
                            draftPhotoPath = newPath,
                            gpsState = if (hasLocationPermissionForNewSave) {
                                GpsCaptureState.Acquiring
                            } else {
                                GpsCaptureState.Error("위치 권한을 허용한 뒤 'GPS 다시 잡기'를 눌러주세요.")
                            }
                        )
                    }
                    if (hasLocationPermissionForNewSave) {
                        acquireGpsForSaveSheet()
                    }
                }
            }
        }
    }

    fun removeDraftPhoto() {
        val old = localState.value.draftPhotoPath
        localState.update { it.copy(draftPhotoPath = null) }
        photoStorageManager.deletePhotoFile(old)
    }

    fun deleteActiveSpotPhoto() {
        val active = uiState.value.activeSpot ?: return
        val oldPhoto = active.photoPath ?: return
        viewModelScope.launch {
            repository.updatePhotoPath(active.id, null)
            photoStorageManager.deletePhotoFile(oldPhoto)
            localState.update { it.copy(previewPhotoPath = null) }
            _messages.tryEmit("기둥 사진이 삭제되었습니다.")
        }
    }

    fun openEditMemoDialog() {
        val active = uiState.value.activeSpot ?: return
        localState.update {
            it.copy(
                isEditMemoDialogOpen = true,
                editMemoText = active.floorZoneMemo
            )
        }
    }

    fun updateEditMemoText(text: String) {
        localState.update { it.copy(editMemoText = text) }
    }

    fun appendQuickChipToEditMemo(chip: String) {
        localState.update { state ->
            val current = state.editMemoText.trim()
            val updated = when {
                current.isEmpty() -> chip
                current.contains(chip) -> current
                else -> "$current $chip"
            }
            state.copy(editMemoText = updated)
        }
    }

    fun dismissEditMemoDialog() {
        localState.update { it.copy(isEditMemoDialogOpen = false) }
    }

    fun saveEditedMemo() {
        val active = uiState.value.activeSpot ?: return
        val newMemo = localState.value.editMemoText
        val reminder = uiState.value.feeReminder
        viewModelScope.launch {
            repository.updateMemo(active.id, newMemo)
            if (reminder.enabled &&
                reminder.spotId == active.id &&
                reminder.triggerAtMillis > System.currentTimeMillis()
            ) {
                ParkingReminderScheduler.scheduleTwoHourReminder(
                    context = appContext,
                    spotId = active.id,
                    memo = newMemo.trim(),
                    savedAtMillis = active.savedAtMillis,
                    triggerAtMillis = reminder.triggerAtMillis
                )
            }
            localState.update { it.copy(isEditMemoDialogOpen = false) }
            _messages.tryEmit("층수/구역 메모가 수정되었습니다.")
        }
    }

    fun selectMapApp(preference: MapAppPreference) {
        viewModelScope.launch {
            preferencesRepository.setPreferredMapApp(preference)
        }
    }

    fun openClearConfirmDialog() {
        localState.update { it.copy(isClearConfirmOpen = true) }
    }

    fun dismissClearConfirmDialog() {
        localState.update { it.copy(isClearConfirmOpen = false) }
    }

    fun confirmClearActiveParking() {
        viewModelScope.launch {
            ParkingReminderScheduler.cancelReminder(appContext)
            preferencesRepository.clearFeeReminder()
            repository.clearActiveParking()
            localState.update { it.copy(isClearConfirmOpen = false) }
            _messages.tryEmit("출차 완료 처리되었습니다.")
        }
    }

    fun openHistorySheet() {
        localState.update { it.copy(isHistorySheetOpen = true) }
    }

    fun dismissHistorySheet() {
        localState.update { it.copy(isHistorySheetOpen = false) }
    }

    fun deleteHistorySpot(spotId: Long) {
        viewModelScope.launch {
            val photoPath = repository.deleteHistorySpot(spotId)
            photoStorageManager.deletePhotoFile(photoPath)
        }
    }

    fun openPhotoPreview(path: String) {
        localState.update { it.copy(previewPhotoPath = path) }
    }

    fun dismissPhotoPreview() {
        localState.update { it.copy(previewPhotoPath = null) }
    }

    fun postMessage(message: String) {
        _messages.tryEmit(message)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appContext = context.applicationContext
            val db = ParkingDatabase.getInstance(appContext)
            return ParkingViewModel(
                appContext = appContext,
                repository = ParkingRepository(db.parkingSpotDao()),
                preferencesRepository = UserPreferencesRepository(appContext),
                locationFetcher = LocationFetcher(appContext),
                photoStorageManager = PhotoStorageManager(appContext),
                initialInstalledMapApps = MapNavigationLauncher.detectInstalledApps(appContext)
            ) as T
        }
    }
}
