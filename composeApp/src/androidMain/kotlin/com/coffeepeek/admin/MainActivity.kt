package com.coffeepeek.admin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null
        val context get() = _context!!
    }

    private val isAppReady = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isAppReady.get() }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _context = this
        setContent {
            App(onReady = { isAppReady.set(true) })
        }
    }

    override fun onDestroy() {
        _context = null
        super.onDestroy()
    }
}
