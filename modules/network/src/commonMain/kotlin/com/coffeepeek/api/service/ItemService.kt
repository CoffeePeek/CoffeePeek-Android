package com.coffeepeek.api.service

import com.coffeepeek.api.model.BaseList
import com.coffeepeek.api.model.request.CreateItemReq
import com.coffeepeek.api.model.response.ItemResp
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.postResult
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

class ItemService(
    private val client: HttpClient
) {


    suspend fun getItems(
        name: String,
        page: Int
    ) = client.getResult("item/list"){
        if (name.isNotBlank()) parameter("name", name)
        parameter("page", page)
    }.getResult<BaseList<ItemResp>>()


    suspend fun getItem(
        itemID: String
    ) = client.getResult("item/$itemID").getResult<ItemResp>()

    suspend fun createItem(
        body: CreateItemReq
    ) = client.postResult("item/create"){
        setJsonBody(body)
    }.getResult<ItemResp>()


    suspend fun editItem(
        id: String,
        body: CreateItemReq
    ) = client.postResult("item/edit/$id"){
        setJsonBody(body)
    }

}