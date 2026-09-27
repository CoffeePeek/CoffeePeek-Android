package com.coffeepeek.data.session

internal expect fun clearPlatformCaches(
    httpCacheFolderPath: String,
    appCacheRootPath: String,
)
