package com.coffeepeek.admin.utils

import android.content.Intent
import com.coffeepeek.admin.locator.Locator

actual object ShareHelper {
    actual fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Поделиться CoffeePeek").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        Locator.appContext.startActivity(chooser)
    }
}
