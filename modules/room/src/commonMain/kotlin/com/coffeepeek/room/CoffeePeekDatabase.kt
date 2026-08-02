@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.coffeepeek.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.coffeepeek.room.dao.BeanBagDao
import com.coffeepeek.room.dao.BrewSessionDao
import com.coffeepeek.room.dao.SettingDAO
import com.coffeepeek.room.entity.BeanBagEntity
import com.coffeepeek.room.entity.BrewSessionEntity
import com.coffeepeek.room.entity.SettingEntity

@Database(
    version = 2,
    entities = [
        SettingEntity::class,
        BeanBagEntity::class,
        BrewSessionEntity::class,
    ],
)
@ConstructedBy(CoffeePeekDatabaseConstructor::class)
abstract class CoffeePeekDatabase : RoomDatabase(), DB {

    abstract val settingDAO: SettingDAO
    abstract val beanBagDao: BeanBagDao
    abstract val brewSessionDao: BrewSessionDao

    companion object {

        fun RoomDatabase.Builder<CoffeePeekDatabase>.configure(): CoffeePeekDatabase {
            return setDriver(BundledSQLiteDriver())
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }

    override fun clearAllTables() {}
}

interface DB {
    fun clearAllTables()
}

@Suppress("KotlinNoActualForExpect")
expect object CoffeePeekDatabaseConstructor : RoomDatabaseConstructor<CoffeePeekDatabase> {
    override fun initialize(): CoffeePeekDatabase
}
