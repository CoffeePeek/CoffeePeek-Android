package com.coffeepeek.admin.config

import com.coffeepeek.BuildConfig

actual object AppConfig {
    actual val versionCode: Long?
        get() {
            val context = com.coffeepeek.admin.locator.Locator.appContext
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            return androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(info)
        }
    actual val updatePlatform: String = "android"
    actual val updateChannel: String?
        get() {
            val context = com.coffeepeek.admin.locator.Locator.appContext
            val installer = try {
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getInstallerPackageName(context.packageName)
                }
            } catch (_: Exception) { null }
            return if (installer == "com.android.vending") "play" else "apk"
        }
    actual val versionName: String = BuildConfig.VERSION_NAME
    actual val baseUrl: String = BuildConfig.API_BASE_URL
    actual val isDebug: Boolean = BuildConfig.DEBUG
}
