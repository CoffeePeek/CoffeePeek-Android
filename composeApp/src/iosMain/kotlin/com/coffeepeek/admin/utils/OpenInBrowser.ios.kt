package com.coffeepeek.admin.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object OpenInBrowser {
    actual fun openInBrowser(link: String) {
        val url = NSURL.URLWithString(link) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
