package com.coffeepeek.api.service

import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.model.response.CodeResp
import com.coffeepeek.api.utils.getNullableResult
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.postResult
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthService(
    private val client: HttpClient
) {

    suspend fun createUser() = client.postResult("auth/create").getResult<AuthResp>()

    suspend fun authRefresh(
        refreshToken: String
    ) = client.getResult("api/Auth/refresh?refreshToken=$refreshToken").getResult<AuthResp>()

    suspend fun authSecret() = client.postResult("auth/secret").getResult<CodeResp>()

    suspend fun authLogin(
        secret: String
    ) = client.postResult("auth/login"){
        setJsonBody(buildJsonObject {
            put("secret", secret)
        })
    }.getNullableResult<AuthResp>()

    suspend fun protected() = client.getResult("auth/protected").map { it.status == HttpStatusCode.OK }

}