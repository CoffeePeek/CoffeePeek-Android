package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.CreateRoasterSubmissionReq
import com.coffeepeek.api.model.response.shop.RoasterDetailsDto
import com.coffeepeek.api.model.response.shop.RoasterSubmissionApiResult
import com.coffeepeek.api.model.response.shop.RoasterSubmissionDto
import com.coffeepeek.api.utils.ApiException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class RoasterApiService(private val client: HttpClient) {

    suspend fun getRoaster(id: String): Result<RoasterDetailsDto> = runCatching {
        val response = client.get("/api/roasters/$id")
        val apiResponse = response.body<ApiResponse<RoasterDetailsDto>>()
        if (!response.status.isSuccess() || !apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.data
    }

    suspend fun submitRoaster(
        request: CreateRoasterSubmissionReq,
    ): Result<RoasterSubmissionApiResult> = runCatching {
        val response = client.post("/api/ModerationRoasters") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val apiResponse = response.body<ApiResponse<RoasterSubmissionDto>>()
        if (!response.status.isSuccess() || !apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        RoasterSubmissionApiResult(apiResponse.data, apiResponse.message)
    }
}
