package com.coffeepeek.admin.utils

import android.content.Intent
import androidx.core.net.toUri
import com.coffeepeek.admin.locator.Locator

actual object OpenInBrowser {

    actual fun openInBrowser(link: String) {
        Locator.activityContext.apply {
            val intent = Intent(Intent.ACTION_VIEW, link.toUri())
            startActivity(intent)
        }
    }
}