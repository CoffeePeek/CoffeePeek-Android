package com.coffeepeek.admin.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import java.nio.channels.FileChannel
import java.util.Locale
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual object ImagePicker {

    @Composable
    actual fun registerInvoker(
        isLoading: (Boolean) -> Unit,
        resultBox: (ByteArray) -> Unit
    ): () -> Unit {
        val scope = rememberCoroutineScope()
        return {
            val dialog = FileDialog(Frame(), "Выберите изображение", FileDialog.LOAD).apply {
                this.accessibleContext
                directory = "."
                file = "*.png;*.jpg;*.jpeg;*.webp;*.gif;*.bmp"
            }
            dialog.isVisible = true
            val file = dialog.file
            val dir = dialog.directory
            if (file != null){
                isLoading(true)
                resultBox(File(dir, file).readBytes())
                isLoading(false)
            }
            dialog.dispose()
        }
    }

}