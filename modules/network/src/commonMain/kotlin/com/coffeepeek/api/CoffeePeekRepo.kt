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
    val itemService = com.coffeepeek.api.service.ItemService(client)
    val fileService = com.coffeepeek.api.service.FileService(client)
}