package com.coffeepeek.data.session

import com.coffeepeek.api.model.response.AuthResp
import kotlinx.coroutines.flow.Flow

interface SessionSecureStore {
    suspend fun read(): AuthResp?
    suspend fun write(data: AuthResp?)
    fun observe(): Flow<AuthResp?>
}
