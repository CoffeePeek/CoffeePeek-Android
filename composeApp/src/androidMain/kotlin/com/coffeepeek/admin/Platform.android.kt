package com.coffeepeek.admin

import android.content.Context
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    val appContext: () -> Context = { FPApplication.context }
    val activityContext: () -> Context = { MainActivity.context }
}

actual fun getPlatform(): Platform = AndroidPlatform()