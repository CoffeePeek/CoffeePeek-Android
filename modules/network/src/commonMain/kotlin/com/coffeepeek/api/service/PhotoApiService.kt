package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.PhotoRequestDto
import com.coffeepeek.api.model.response.GenerateUploadUrlDto
import com.coffeepeek.api.utils.ApiException
import com.coffeepeek.api.utils.postResult
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class PhotoApiService(
    private val client: HttpClient,
    private val uploadClient: HttpClient,
) {

    suspend fun requestShopPhotoUploadUrls(
        requests: List<PhotoRequestDto>,
    ): Result<List<GenerateUploadUrlDto>> = runCatching {
        val response = client.postResult("/api/Photos/shop") {
            setJsonBody(requests)
        }.getOrThrow()
        if (!response.status.isSuccess()) {
            throw ApiException("Не удалось загрузить фото (${response.status.value})")
        }
        val apiResponse = response.body<ApiResponse<List<GenerateUploadUrlDto>>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.data
    }

    suspend fun uploadToPresignedUrl(
        uploadUrl: String,
        bytes: ByteArray,
        contentType: String,
    ): Result<Unit> = runCatching {
        val response = uploadClient.put(uploadUrl) {
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }
        if (!response.status.isSuccess()) {
            throw ApiException("Ошибка загрузки фото (${response.status.value})")
        }
    }
}
