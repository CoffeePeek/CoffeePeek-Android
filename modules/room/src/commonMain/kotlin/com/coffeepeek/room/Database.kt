package com.coffeepeek.room

import com.coffeepeek.room.dao.BeanBagDao
import com.coffeepeek.room.dao.BrewSessionDao
import com.coffeepeek.room.repository.SettingRepository

object Database

interface DatabaseCore {
    val settingRepository: SettingRepository
    val beanBagDao: BeanBagDao
    val brewSessionDao: BrewSessionDao
}
