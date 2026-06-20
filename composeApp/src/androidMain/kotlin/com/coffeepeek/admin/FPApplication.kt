package com.coffeepeek.admin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.coffeepeek.BuildConfig
import com.yandex.mapkit.MapKitFactory

class FPApplication: Application() {

    companion object{

        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null
        val context get() = _context!!

    }

    override fun onCreate() {
        super.onCreate()
        _context = this
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        com.coffeepeek.admin.di.initPlatformKoin()
    }

    override fun onTerminate() {
        _context = null
        super.onTerminate()
    }


}