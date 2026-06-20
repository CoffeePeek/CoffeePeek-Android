package com.coffeepeek.admin.locator

import com.coffeepeek.admin.FPApplication
import com.coffeepeek.admin.utils.CustomUrlFetcher
import org.example.project.Database
import org.example.project.DatabaseCore
import org.example.project.MyRoomRepositoryImpl.Companion.database
import java.io.File

actual object Locator : ALocator() {

    val appContext get() = FPApplication.context

    actual override val cacheFolder: File by lazy { File(appContext.cacheDir, "cache").apply { mkdirs() } }

    actual override val database: DatabaseCore by lazy { Database.database(appContext, Constants.DB_NAME) }

    actual override val urlFetcher: CustomUrlFetcher by lazy { CustomUrlFetcher(compress = 100..500, fpClient.client) }

}