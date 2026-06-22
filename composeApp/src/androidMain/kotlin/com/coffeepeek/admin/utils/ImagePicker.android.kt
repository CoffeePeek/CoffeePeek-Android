package com.coffeepeek.admin.utils

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.coffeepeek.admin.utils.BitmapUtil.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberPhotoPicker(
    maxSelection: Int,
    isLoading: (Boolean) -> Unit,
    onPhotosPicked: (List<PickedImage>) -> Unit,
): PhotoPickerController {
    val activity = LocalActivity.current
    val resolver = activity?.contentResolver
    val scope = rememberCoroutineScope()
    val maxSelectionState = rememberUpdatedState(maxSelection.coerceIn(1, MAX_SHOP_PHOTOS))
    val onPhotosPickedState = rememberUpdatedState(onPhotosPicked)
    val isLoadingState = rememberUpdatedState(isLoading)

    fun processUris(uris: List<Uri>) {
        if (uris.isEmpty() || resolver == null) return
        scope.launch(Dispatchers.IO) {
            isLoadingState.value(true)
            val limit = maxSelectionState.value
            val images = uris.take(limit).mapNotNull { uri ->
                runCatching {
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@mapNotNull null
                    val compressed = BitmapUtil.load(bytes).toByteArray()
                    val fileName = resolver.queryFileName(uri) ?: "photo_${System.currentTimeMillis()}.jpg"
                    PickedImage(bytes = compressed, fileName = fileName, contentType = "image/jpeg")
                }.getOrNull()
            }
            if (images.isNotEmpty()) {
                withContext(Dispatchers.Main) { onPhotosPickedState.value(images) }
            }
            isLoadingState.value(false)
        }
    }

    val pickMultiple = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_SHOP_PHOTOS),
    ) { uris -> processUris(uris) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            isLoadingState.value(true)
            val bytes = bitmap.toJpegBytes()
            val image = PickedImage(
                bytes = bytes,
                fileName = "photo_${System.currentTimeMillis()}.jpg",
                contentType = "image/jpeg",
            )
            withContext(Dispatchers.Main) { onPhotosPickedState.value(listOf(image)) }
            isLoadingState.value(false)
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) takePicture.launch(null)
    }

    fun launchCamera() {
        val context = activity ?: return
        when {
            context.checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> takePicture.launch(null)
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    return PhotoPickerController(
        pickFromGallery = {
            pickMultiple.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build(),
            )
        },
        takePhoto = ::launchCamera,
    )
}

private fun android.content.ContentResolver.queryFileName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }

private fun Bitmap.toJpegBytes(quality: Int = 85): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}
