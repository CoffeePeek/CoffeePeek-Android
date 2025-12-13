package org.example.project

import org.example.project.repository.SettingRepository
import org.example.project.repository.SettingRepositoryImp


expect class MyRoomRepositoryImpl: FPDatabaseRepository

abstract class FPDatabaseRepository(
    myRoomDatabase: FPDatabase
) : DatabaseCore{

    override val settingRepository: SettingRepository = SettingRepositoryImp(myRoomDatabase.settingDAO)

}