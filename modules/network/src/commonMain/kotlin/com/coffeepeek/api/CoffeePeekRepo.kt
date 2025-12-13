package com.coffeepeek.api

import com.coffeepeek.api.service.AuthService
import com.coffeepeek.api.service.FileService
import com.coffeepeek.api.service.ItemService

class CoffeePeekRepo(httpClient: CoffeePeekClient) {

    private val client = httpClient.client

    val authService = AuthService(client)

    val itemService = ItemService(client)

    val fileService = FileService(client)

}