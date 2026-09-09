package com.coffeepeek.api.service

import com.coffeepeek.api.model.ApiResponse
import com.coffeepeek.api.model.request.CreateShopIssueReportReq
import com.coffeepeek.api.utils.ApiException
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

class ShopIssueReportApiService(private val client: HttpClient) {

    suspend fun submitReport(req: CreateShopIssueReportReq): Result<String?> = runCatching {
        val response = client.post("/api/ShopIssueReports") {
            setJsonBody(req)
        }
        val apiResponse = response.body<ApiResponse<Unit>>()
        if (!apiResponse.isSuccess) {
            throw ApiException(apiResponse.message)
        }
        apiResponse.entityId
    }
}
