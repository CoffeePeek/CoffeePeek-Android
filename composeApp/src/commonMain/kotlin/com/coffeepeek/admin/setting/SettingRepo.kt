package com.coffeepeek.admin.setting

import com.coffeepeek.api.mapper.UserMapper.toUser
import com.coffeepeek.api.model.response.AuthResp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.example.project.DatabaseCore
import org.example.project.repository.readSerializable
import org.example.project.repository.readSerializableFlow
import org.example.project.repository.saveSerializable

class SettingRepo(
    database: DatabaseCore,
    private val preferenceScope: CoroutineScope
) {

    companion object {
        private const val API_KEY = "API_KEYS"

    }


    val setAuth: suspend (AuthResp?) -> Unit = { database.settingRepository.saveSerializable(API_KEY, it) }
    val getAuth: suspend () -> AuthResp? = { database.settingRepository.readSerializable(API_KEY) }
    val authFlow = database.settingRepository.readSerializableFlow<AuthResp>(API_KEY).asState(runBlocking { getAuth() })
    val userFlow = authFlow.map { it?.toUser() }.asState(runBlocking { getAuth()?.toUser() })


    private fun <T> Flow<T>.asState(default: T) = stateIn(preferenceScope, SharingStarted.Eagerly, default)
}