package com.coffeepeek.admin.locator

import com.coffeepeek.room.DatabaseCore
import java.io.File

expect object Locator {
    val cacheFolder: File
    val database: DatabaseCore
}
