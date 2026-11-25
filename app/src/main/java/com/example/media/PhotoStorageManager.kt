package com.example.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PendingPhotoCapture(
    val filePath: String,
    val contentUri: Uri
)

class PhotoStorageManager(private val context: Context) {
    private val photosDir: File
        get() = File(context.filesDir, "parking_photos").apply {
            if (!exists()) mkdirs()
        }

    fun createCaptureDestination(): PendingPhotoCapture {
        val fileName = "pillar_${System.currentTimeMillis()}.jpg"
        val file = File(photosDir, fileName)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return PendingPhotoCapture(
            filePath = file.absolutePath,
            contentUri = uri
        )
    }

    suspend fun copyFromUri(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = "pillar_pick_${System.currentTimeMillis()}.jpg"
            val destFile = File(photosDir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            if (destFile.length() > 0L) destFile.absolutePath else null
        }.getOrNull()
    }

    fun deletePhotoFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
