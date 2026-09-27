package com.coffeepeek.core.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

// These schemas are test-only; no application entity/DAO belongs in core.
@Entity(tableName = "entries")
data class EntryV1(@PrimaryKey val id: Int, val value: String)

@Entity(tableName = "entries")
data class EntryV2(
    @PrimaryKey val id: Int,
    val value: String,
    @ColumnInfo(defaultValue = "'untitled'") val label: String = "untitled",
)

@Dao
interface EntryV1Dao {
    @Insert suspend fun insert(entry: EntryV1)
    @Query("SELECT * FROM entries ORDER BY id") suspend fun entries(): List<EntryV1>
}

@Dao
interface EntryV2Dao {
    @Query("SELECT * FROM entries ORDER BY id") suspend fun entries(): List<EntryV2>
}

@Database(entities = [EntryV1::class], version = 1, exportSchema = false)
abstract class FixtureDatabaseV1 : RoomDatabase() {
    abstract fun entries(): EntryV1Dao
}

@Database(entities = [EntryV2::class], version = 2, exportSchema = false)
abstract class FixtureDatabaseV2 : RoomDatabase() {
    abstract fun entries(): EntryV2Dao
}

val fixtureMigration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.prepare("ALTER TABLE entries ADD COLUMN label TEXT NOT NULL DEFAULT 'untitled'").use {
            it.step()
        }
    }
}
