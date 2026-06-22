package com.coffeepeek.admin.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberPhotoPicker(
    maxSelection: Int,
    isLoading: (Boolean) -> Unit,
    onPhotosPicked: (List<PickedImage>) -> Unit,
): PhotoPickerController {
    val maxSelectionState = rememberUpdatedState(maxSelection.coerceIn(1, MAX_SHOP_PHOTOS))
    val onPhotosPickedState = rememberUpdatedState(onPhotosPicked)
    val isLoadingState = rememberUpdatedState(isLoading)

    return remember {
        PhotoPickerController(
            pickFromGallery = {
                val chooser = JFileChooser().apply {
                    isMultiSelectionEnabled = true
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    fileFilter = FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "webp", "gif", "bmp")
                }
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return@PhotoPickerController
                val files = chooser.selectedFiles
                    .take(maxSelectionState.value)
                    .filter { it.isFile }
                if (files.isEmpty()) return@PhotoPickerController
                isLoadingState.value(true)
                val images = files.map { file ->
                    PickedImage(
                        bytes = file.readBytes(),
                        fileName = file.name,
                        contentType = guessContentType(file.name),
                    )
                }
                onPhotosPickedState.value(images)
                isLoadingState.value(false)
            },
            takePhoto = {
                val dialog = FileDialog(Frame(), "Выберите изображение", FileDialog.LOAD).apply {
                    directory = "."
                    file = "*.png;*.jpg;*.jpeg;*.webp"
                }
                dialog.isVisible = true
                val fileName = dialog.file
                val dir = dialog.directory
                dialog.dispose()
                if (fileName == null || dir == null) return@PhotoPickerController
                val file = File(dir, fileName)
                isLoadingState.value(true)
                onPhotosPickedState.value(
                    listOf(
                        PickedImage(
                            bytes = file.readBytes(),
                            fileName = file.name,
                            contentType = guessContentType(file.name),
                        )
                    )
                )
                isLoadingState.value(false)
            },
        )
    }
}

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
    else -> "image/jpeg"
}
