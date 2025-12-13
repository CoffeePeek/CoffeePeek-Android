package com.coffeepeek.admin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class FPApplication: Application() {

    companion object{

        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null
        val context get() = _context!!

    }

    override fun onCreate() {
        _context = this
        super.onCreate()
    }

    override fun onTerminate() {
        _context = null
        super.onTerminate()
    }


}