package com.coffeepeek.admin.locator

import org.example.project.DatabaseCore
import org.example.project.MyRoomRepositoryImpl
import java.io.File

actual object Locator {
    actual val cacheFolder: File by lazy { File("cache").apply { mkdirs() } }

    actual val database: DatabaseCore by lazy { MyRoomRepositoryImpl(Constants.DB_NAME) }
}
