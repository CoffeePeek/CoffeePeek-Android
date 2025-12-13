package org.example.project.mapper

import org.example.project.entity.SettingEntity
import org.example.project.model.Setting

object SettingMapper {


    fun Setting.toEntity() = SettingEntity(
        key = key,
        value = value
    )

    fun SettingEntity.toDTO() = Setting(
        key = key,
        value = value
    )

}