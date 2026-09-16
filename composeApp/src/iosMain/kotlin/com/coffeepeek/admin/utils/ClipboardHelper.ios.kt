package com.coffeepeek.admin.utils

import platform.UIKit.UIPasteboard

actual object ClipboardHelper {
    actual fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
