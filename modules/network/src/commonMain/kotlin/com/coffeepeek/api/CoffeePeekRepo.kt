package com.coffeepeek.api

import com.coffeepeek.api.service.AuthService
import com.coffeepeek.api.service.FileService
import com.coffeepeek.api.service.ItemService
import com.coffeepeek.api.service.UserApiService

class CoffeePeekRepo(httpClient: CoffeePeekClient) {

    val authService = httpClient.authService
    private val client = httpClient.client

    val shopApiService = com.coffeepeek.api.service.ShopApiService(client)
    val userApiService = UserApiService(client)
    val photoApiService = com.coffeepeek.api.service.PhotoApiService(client, httpClient.uploadClient)
    val favoriteApiService = com.coffeepeek.api.service.FavoriteApiService(client)
    val reviewApiService = com.coffeepeek.api.service.ReviewApiService(client)
    val checkInApiService = com.coffeepeek.api.service.CheckInApiService(client)
    val itemService = com.coffeepeek.api.service.ItemService(client)
    val fileService = com.coffeepeek.api.service.FileService(client)
}