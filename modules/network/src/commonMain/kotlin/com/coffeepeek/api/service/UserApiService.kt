package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.UpdateAboutReq
import com.coffeepeek.api.model.request.UpdateUsernameReq
import com.coffeepeek.api.model.response.UserProfileDto
import com.coffeepeek.api.utils.ApiException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class UserApiService(private val client: HttpClient) {

    suspend fun getMe(): Result<UserProfileDto> = runCatching {
        val response = client.get("/api/Users/me")
        val apiResponse = response.body<ApiResponse<UserProfileDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data
    }

    suspend fun updateUsername(username: String): Result<Unit> = runCatching {
        val response = client.patch("/api/Users/me/username") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUsernameReq(username))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ApiResponse<Unit>>() }.getOrNull()
            throw ApiException(err?.message ?: "Ошибка обновления имени (${response.status.value})")
        }
    }

    suspend fun updateAbout(about: String): Result<Unit> = runCatching {
        val response = client.patch("/api/Users/me/about") {
            contentType(ContentType.Application.Json)
            setBody(UpdateAboutReq(about))
        }
        if (!response.status.isSuccess()) {
            val err = runCatching { response.body<ApiResponse<Unit>>() }.getOrNull()
            throw ApiException(err?.message ?: "Ошибка обновления описания (${response.status.value})")
        }
    }
}
