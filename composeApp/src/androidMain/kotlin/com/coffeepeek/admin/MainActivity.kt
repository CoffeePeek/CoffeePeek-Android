package com.coffeepeek.admin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _context: Context? = null
        val context get() = _context!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _context = this
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        _context = null
        super.onDestroy()
    }
}
