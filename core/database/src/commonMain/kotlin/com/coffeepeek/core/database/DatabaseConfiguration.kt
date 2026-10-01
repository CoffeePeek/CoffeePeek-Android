package com.coffeepeek.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** Configures the shared driver; schema, migrations and lifecycle belong to the caller. */
fun <T : RoomDatabase> RoomDatabase.Builder<T>.configureDatabaseDriver(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver())
