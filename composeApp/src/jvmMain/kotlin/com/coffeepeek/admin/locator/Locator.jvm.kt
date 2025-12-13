package com.coffeepeek.admin.locator

import com.coffeepeek.admin.utils.CustomUrlFetcher
import org.example.project.Database
import org.example.project.DatabaseCore
import org.example.project.MyRoomRepositoryImpl.Companion.database
import java.io.File

actual object Locator: ALocator() {


    actual override val cacheFolder: File by lazy { File("cache").apply { mkdirs() } }

    actual override val database: DatabaseCore by lazy { Database.database(Constants.DB_NAME) }


    actual override val urlFetcher: CustomUrlFetcher by lazy { CustomUrlFetcher(fpClient.client) }

}