package com.coffeepeek.admin.utils

import androidx.compose.runtime.Composable
import java.io.File

expect object ImagePicker {


    @Composable
    fun registerInvoker(
        isLoading: (Boolean) -> Unit = {},
        resultBox: (ByteArray) -> Unit
    ): () -> Unit

}