package com.coffeepeek.admin.utils

import java.awt.Desktop
import java.net.URI
import java.util.Locale

actual object OpenInBrowser {

    
    actual fun openInBrowser(link: String) {
        val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.getDefault()) }
        val desktop = Desktop.getDesktop()

        when {
            Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> desktop.browse(URI.create(link))
            "mac" in osName -> Runtime.getRuntime().exec(arrayOf("open", link))
            "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec(arrayOf("xdg-open", link))
            else -> throw RuntimeException("cannot open $link")
        }
    }
}