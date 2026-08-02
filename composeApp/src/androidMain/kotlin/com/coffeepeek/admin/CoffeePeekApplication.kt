package com.coffeepeek.admin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.coffeepeek.BuildConfig
import com.coffeepeek.admin.di.initPlatformKoin
import com.coffeepeek.domain.repository.SessionRepository
import com.yandex.mapkit.MapKitFactory
import io.kamel.core.config.KamelConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class CoffeePeekApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null
        val context get() = _context!!
    }

    private val warmupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        _context = this
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        initPlatformKoin()
        warmupScope.launch {
            val koin = GlobalContext.get()
            runCatching { koin.get<SessionRepository>().warmCache() }
            runCatching { koin.get<KamelConfig>() }
        }
    }

    override fun onTerminate() {
        _context = null
        super.onTerminate()
    }
}
