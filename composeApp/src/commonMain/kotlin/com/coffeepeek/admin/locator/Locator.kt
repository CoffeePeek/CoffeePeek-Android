package com.coffeepeek.admin.locator

import org.example.project.DatabaseCore
import java.io.File

expect object Locator {
    val cacheFolder: File
    val database: DatabaseCore
}
