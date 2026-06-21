package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.CreateShopReq
import com.coffeepeek.api.model.response.shop.CatalogItemDto
import com.coffeepeek.api.model.response.shop.CityItemDto
import com.coffeepeek.api.model.response.shop.CoffeeShopDetailsDto
import com.coffeepeek.api.model.response.shop.GetBeansResponseDto
import com.coffeepeek.api.model.response.shop.GetBrewMethodsResponseDto
import com.coffeepeek.api.model.response.shop.GetCitiesResponseDto
import com.coffeepeek.api.model.response.shop.GetEquipmentResponseDto
import com.coffeepeek.api.model.response.shop.GetRoastersResponseDto
import com.coffeepeek.api.model.response.shop.GetShopDetailsResponseDto
import com.coffeepeek.api.model.response.shop.GetShopsInBoundsResponseDto
import com.coffeepeek.api.model.response.shop.MapShopDto
import com.coffeepeek.api.model.response.shop.GetShopsResponseDto
import com.coffeepeek.api.utils.ApiException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class ShopApiService(private val client: HttpClient) {

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun searchShops(
        query: String? = null,
        cityId: String? = null,
        roasterIds: List<String>? = null,
        equipmentIds: List<String>? = null,
        beanIds: List<String>? = null,
        brewMethodIds: List<String>? = null,
        priceRange: Int? = null,
        minRating: Double? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<GetShopsResponseDto> = runCatching {
        val response = client.get("/api/CoffeeShops") {
            query?.let { parameter("q", it) }
            cityId?.let { parameter("cityId", it) }
            roasterIds?.forEach { parameter("roasters", it) }
            equipmentIds?.forEach { parameter("equipments", it) }
            beanIds?.forEach { parameter("beans", it) }
            brewMethodIds?.forEach { parameter("brewMethods", it) }
            priceRange?.let { parameter("priceRange", it) }
            minRating?.let { parameter("minRating", it) }
            parameter("page", page)
            parameter("pageSize", pageSize)
        }
        val apiResponse = response.body<ApiResponse<GetShopsResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data
    }

    suspend fun getShopDetails(id: String): Result<CoffeeShopDetailsDto> = runCatching {
        val response = client.get("/api/CoffeeShops/$id")
        val apiResponse = response.body<ApiResponse<GetShopDetailsResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.shopDto
    }

    // ── Create ────────────────────────────────────────────────────────────────

    suspend fun createShop(req: CreateShopReq): Result<Unit> = runCatching {
        val response = client.post("/api/ModerationShops") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (!response.status.isSuccess()) {
            val apiResponse = runCatching { response.body<ApiResponse<Unit>>() }.getOrNull()
            throw ApiException(apiResponse?.message ?: "Ошибка создания кофейни (${response.status.value})")
        }
    }

    // ── Catalogs ──────────────────────────────────────────────────────────────

    suspend fun getCities(): Result<List<CityItemDto>> = runCatching {
        val response = client.get("/api/Catalogs/cities")
        val apiResponse = response.body<ApiResponse<GetCitiesResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.cities
    }

    suspend fun getBeans(): Result<List<CatalogItemDto>> = runCatching {
        val response = client.get("/api/Catalogs/beans")
        val apiResponse = response.body<ApiResponse<GetBeansResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.beans
    }

    suspend fun getEquipment(): Result<List<CatalogItemDto>> = runCatching {
        val response = client.get("/api/Catalogs/equipments")
        val apiResponse = response.body<ApiResponse<GetEquipmentResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.equipments
    }

    suspend fun getRoasters(): Result<List<CatalogItemDto>> = runCatching {
        val response = client.get("/api/Catalogs/roasters")
        val apiResponse = response.body<ApiResponse<GetRoastersResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.roasters
    }

    suspend fun getBrewMethods(): Result<List<CatalogItemDto>> = runCatching {
        val response = client.get("/api/Catalogs/brew-methods")
        val apiResponse = response.body<ApiResponse<GetBrewMethodsResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.brewMethods
    }

    suspend fun getShopsInBounds(
        minLat: Double,
        minLon: Double,
        maxLat: Double,
        maxLon: Double,
    ): Result<List<MapShopDto>> = runCatching {
        val response = client.get("/api/Map") {
            parameter("minLat", minLat)
            parameter("minLon", minLon)
            parameter("maxLat", maxLat)
            parameter("maxLon", maxLon)
        }
        val apiResponse = response.body<ApiResponse<GetShopsInBoundsResponseDto>>()
        if (!apiResponse.isSuccess || apiResponse.data == null) throw ApiException(apiResponse.message)
        apiResponse.data.shops
    }
}
