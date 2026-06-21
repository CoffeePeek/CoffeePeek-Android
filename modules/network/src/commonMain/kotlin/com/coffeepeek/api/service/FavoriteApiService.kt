package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.response.GetAllFavoritesResponseDto
import com.coffeepeek.api.utils.ApiException
import com.coffeepeek.api.utils.deleteResult
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.postResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

class FavoriteApiService(private val client: HttpClient) {

    suspend fun getFavorites(): Result<GetAllFavoritesResponseDto> = runCatching {
        val response = client.getResult("/api/FavoriteCoffeeShops").getOrThrow()
        val apiResponse = response.body<ApiResponse<GetAllFavoritesResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.data
    }

    suspend fun addFavorite(shopId: String): Result<Unit> = runCatching {
        val response = client.postResult("/api/FavoriteCoffeeShops") {
            parameter("id", shopId)
        }.getOrThrow()
        if (!response.status.isSuccess()) {
            val apiResponse = runCatching { response.body<ApiResponse<*>>() }.getOrNull()
            throw ApiException(apiResponse?.message ?: "Ошибка добавления в избранное")
        }
    }

    suspend fun removeFavorite(shopId: String): Result<Unit> = runCatching {
        val response = client.deleteResult("/api/FavoriteCoffeeShops") {
            parameter("id", shopId)
        }.getOrThrow()
        if (!response.status.isSuccess()) {
            val apiResponse = runCatching { response.body<ApiResponse<*>>() }.getOrNull()
            throw ApiException(apiResponse?.message ?: "Ошибка удаления из избранного")
        }
    }
}
