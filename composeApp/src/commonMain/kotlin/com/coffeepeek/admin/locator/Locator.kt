package com.coffeepeek.admin.locator

import com.coffeepeek.api.CoffeePeekClient
import com.coffeepeek.api.CoffeePeekRepo
import com.coffeepeek.admin.setting.SettingRepo
import com.coffeepeek.admin.utils.CustomUrlFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.example.project.DatabaseCore
import java.io.File

expect object Locator : ALocator {

    override val cacheFolder: File

    override val database: DatabaseCore

    override val urlFetcher: CustomUrlFetcher


}

abstract class ALocator {

    abstract val cacheFolder: File

    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    abstract val database: DatabaseCore

    val setting by lazy { SettingRepo(database, applicationScope) }

    val fpClient by lazy {
        CoffeePeekClient(
            url = Constants.DOMAIN_URL,
            getToken = { runBlocking { setting.getAuth() } },
            saveToken = { runBlocking { setting.setAuth(it) } },
            debug = true,
            cacheFolder = cacheFolder
        )
    }

    val repo by lazy { CoffeePeekRepo(fpClient) }

    abstract val urlFetcher: CustomUrlFetcher
}