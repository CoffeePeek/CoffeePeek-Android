package com.coffeepeek.data.di

import com.coffeepeek.api.CoffeePeekClient
import com.coffeepeek.api.CoffeePeekRepo
import com.coffeepeek.api.service.AuthService
import com.coffeepeek.data.repository.AuthRepositoryImpl
import com.coffeepeek.data.repository.SessionRepositoryImpl
import com.coffeepeek.data.repository.ShopRepositoryImpl
import com.coffeepeek.data.repository.UserRepositoryImpl
import com.coffeepeek.domain.repository.AuthRepository
import com.coffeepeek.domain.repository.SessionRepository
import com.coffeepeek.domain.repository.ShopRepository
import com.coffeepeek.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.example.project.DatabaseCore
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

fun dataModule(
    baseUrl: String,
    cacheFolder: File,
    database: DatabaseCore,
    debug: Boolean = false,
): Module = module {
    single { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    single {
        CoffeePeekClient(
            url = baseUrl,
            cacheFolder = cacheFolder,
            debug = debug,
            getToken = {
                runBlocking {
                    get<SessionRepository>().getSession()?.let { session ->
                        com.coffeepeek.api.model.response.AuthResp(
                            accessToken = session.accessToken,
                            refreshToken = session.refreshToken.orEmpty(),
                        )
                    }
                }
            },
            saveToken = { authResp ->
                runBlocking {
                    val session = authResp?.let {
                        com.coffeepeek.domain.model.Session(
                            accessToken = it.accessToken,
                            refreshToken = it.refreshToken.takeIf { token -> token.isNotBlank() },
                        )
                    }
                    get<SessionRepository>().saveSession(session)
                }
            },
        )
    }

    single { CoffeePeekRepo(get()) }
    single { get<CoffeePeekRepo>().authService }
    single { get<CoffeePeekRepo>().shopApiService }
    single { get<CoffeePeekRepo>().userApiService }
    single<SessionRepository> { SessionRepositoryImpl(database, get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
}
