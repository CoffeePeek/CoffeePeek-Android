package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.SendReviewReq
import com.coffeepeek.api.model.request.UpdateReviewReq
import com.coffeepeek.api.model.response.CanCreateReviewResponseDto
import com.coffeepeek.api.model.response.CreateEntityResponseDto
import com.coffeepeek.api.model.response.GetReviewsByUserIdResponseDto
import com.coffeepeek.api.utils.ApiException
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.postResult
import com.coffeepeek.api.utils.putResult
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter

class ReviewApiService(private val client: HttpClient) {

    suspend fun canCreateReview(shopId: String): Result<CanCreateReviewResponseDto> = runCatching {
        val response = client.getResult("/api/CoffeeShopReviews/can-create") {
            parameter("shopId", shopId)
        }.getOrThrow()
        val apiResponse = response.body<ApiResponse<CanCreateReviewResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.data
    }

    suspend fun createReview(req: SendReviewReq): Result<CreateEntityResponseDto> = runCatching {
        val response = client.postResult("/api/ModerationReviews") {
            setJsonBody(req)
        }.getOrThrow()
        val apiResponse = response.body<ApiResponse<CreateEntityResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.data
    }

    suspend fun updateReview(reviewId: String, req: UpdateReviewReq): Result<Unit> = runCatching {
        val response = client.putResult("/api/ModerationReviews/$reviewId") {
            setJsonBody(req)
        }.getOrThrow()
        val apiResponse = response.body<ApiResponse<Unit>>()
        if (!apiResponse.isSuccess) {
            throw ApiException(apiResponse.message)
        }
    }

    suspend fun getUserReviews(userId: String, page: Int, pageSize: Int): Result<GetReviewsByUserIdResponseDto> =
        runCatching {
            val response = client.getResult("/api/users/$userId/reviews") {
                parameter("pageNumber", page)
                parameter("pageSize", pageSize)
            }.getOrThrow()
            val apiResponse = response.body<ApiResponse<GetReviewsByUserIdResponseDto>>()
            if (!apiResponse.isSuccess || apiResponse.data == null) {
                throw ApiException(apiResponse.message)
            }
            apiResponse.data
        }
}
