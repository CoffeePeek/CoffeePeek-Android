package com.coffeepeek.data.session

import java.io.File

internal actual fun clearPlatformImageCaches(appCacheRoot: File) {
    appCacheRoot.listFiles()?.forEach { entry ->
        if (entry.name.equals("kamel", ignoreCase = true) ||
            entry.name.contains("image", ignoreCase = true)
        ) {
            entry.deleteRecursively()
        }
    }
}
