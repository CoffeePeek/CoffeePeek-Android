package com.coffeepeek.admin.utils

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.coffeepeek.admin.utils.BitmapUtil.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

actual object ImagePicker {


//    actual fun pickImage(onImageSelected: (File) -> Unit) {
//
//    }


    @Composable
    actual fun registerInvoker(
        isLoading: (Boolean) -> Unit,
        resultBox: (ByteArray) -> Unit
    ): () -> Unit {
        val resolver = LocalActivity.current?.contentResolver
        val scope = rememberCoroutineScope()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
            result ?: return@rememberLauncherForActivityResult
            resolver ?: return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                isLoading(true)
                resolver.openInputStream(result)
                    ?.readBytes()
                    ?.let { BitmapUtil.load(it).toByteArray() }
                    ?.let(resultBox)
                isLoading(false)
            }
        }
        return {
            val type = PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
            launcher.launch(type)
        }
    }


}