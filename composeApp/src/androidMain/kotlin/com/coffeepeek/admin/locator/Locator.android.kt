package com.coffeepeek.admin.locator

import com.coffeepeek.admin.FPApplication
import org.example.project.DatabaseCore
import org.example.project.MyRoomRepositoryImpl
import java.io.File

actual object Locator {
    val appContext get() = FPApplication.context

    actual val cacheFolder: File by lazy { File(appContext.cacheDir, "cache").apply { mkdirs() } }

    actual val database: DatabaseCore by lazy {
        MyRoomRepositoryImpl(appContext, Constants.DB_NAME)
    }
}
