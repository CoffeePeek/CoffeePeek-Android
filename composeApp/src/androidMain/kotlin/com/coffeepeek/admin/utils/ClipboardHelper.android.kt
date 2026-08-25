package com.coffeepeek.admin.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.coffeepeek.admin.locator.Locator

actual object ClipboardHelper {
    actual fun copyText(text: String) {
        val clipboard = Locator.appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("phone", text))
    }
}
