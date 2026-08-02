package com.coffeepeek.room

import com.coffeepeek.room.dao.BeanBagDao
import com.coffeepeek.room.dao.BrewSessionDao
import com.coffeepeek.room.repository.SettingRepository
import com.coffeepeek.room.repository.SettingRepositoryImp

expect class MyRoomRepositoryImpl : CoffeePeekDatabaseRepository

abstract class CoffeePeekDatabaseRepository(
    database: CoffeePeekDatabase,
) : DatabaseCore {

    override val settingRepository: SettingRepository = SettingRepositoryImp(database.settingDAO)
    override val beanBagDao: BeanBagDao = database.beanBagDao
    override val brewSessionDao: BrewSessionDao = database.brewSessionDao
}
