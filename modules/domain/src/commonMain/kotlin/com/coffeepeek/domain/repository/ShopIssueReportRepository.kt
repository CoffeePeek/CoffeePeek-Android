package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.ShopIssueCategory

interface ShopIssueReportRepository {
    suspend fun submitReport(shopId: String, category: ShopIssueCategory, description: String?): Result<Unit>
}
