package com.coffeepeek.data.session

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.room.repository.SettingRepository
import com.coffeepeek.room.repository.readSerializable
import com.coffeepeek.room.repository.readSerializableFlow
import com.coffeepeek.room.repository.saveSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Plaintext fallback for non-Android targets and tests.
 */
class RoomSessionSecureStore(
    private val settings: SettingRepository,
) : SessionSecureStore {

    override suspend fun read(): AuthResp? =
        settings.readSerializable(SESSION_KEY)

    override suspend fun write(data: AuthResp?) {
        settings.saveSerializable(SESSION_KEY, data)
    }

    override fun observe(): Flow<AuthResp?> =
        settings.readSerializableFlow(SESSION_KEY)

    companion object {
        const val SESSION_KEY = "SESSION"
    }
}
