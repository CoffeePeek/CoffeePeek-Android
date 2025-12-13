package org.example.project

import org.example.project.repository.SettingRepository


object Database


interface DatabaseCore {

    val settingRepository: SettingRepository

}