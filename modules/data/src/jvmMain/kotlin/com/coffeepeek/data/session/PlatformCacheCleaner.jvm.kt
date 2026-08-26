package com.coffeepeek.data.session

import java.io.File

internal actual fun clearPlatformImageCaches(appCacheRoot: File) {
    // No-op on non-Android targets for now.
}
