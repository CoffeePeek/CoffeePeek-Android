package com.coffeepeek.admin.locator

import com.coffeepeek.admin.CoffeePeekApplication
import com.coffeepeek.room.DatabaseCore
import com.coffeepeek.room.MyRoomRepositoryImpl
import java.io.File

actual object Locator {
    val appContext get() = CoffeePeekApplication.context

    actual val cacheFolder: File by lazy { File(appContext.cacheDir, "cache").apply { mkdirs() } }

    actual val database: DatabaseCore by lazy {
        MyRoomRepositoryImpl(appContext, Constants.DB_NAME)
    }
}
