package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateShopIssueReportReq
import com.coffeepeek.api.service.ShopIssueReportApiService
import com.coffeepeek.domain.model.ShopIssueCategory
import com.coffeepeek.domain.repository.ShopIssueReportRepository
import com.coffeepeek.api.model.request.ShopIssueCategory as NetworkShopIssueCategory

class ShopIssueReportRepositoryImpl(
    private val shopIssueReportApiService: ShopIssueReportApiService,
) : ShopIssueReportRepository {

    override suspend fun submitReport(
        shopId: String,
        category: ShopIssueCategory,
        description: String?,
    ): Result<Unit> = shopIssueReportApiService.submitReport(
        CreateShopIssueReportReq(
            shopId = shopId,
            category = category.toNetwork(),
            description = description,
        ),
    ).map { }

    private fun ShopIssueCategory.toNetwork(): NetworkShopIssueCategory = when (this) {
        ShopIssueCategory.OutdatedMenu -> NetworkShopIssueCategory.OutdatedMenu
        ShopIssueCategory.ShopClosed -> NetworkShopIssueCategory.ShopClosed
        ShopIssueCategory.IncorrectAddress -> NetworkShopIssueCategory.IncorrectAddress
        ShopIssueCategory.WrongOpeningHours -> NetworkShopIssueCategory.WrongOpeningHours
        ShopIssueCategory.IncorrectPhotos -> NetworkShopIssueCategory.IncorrectPhotos
        ShopIssueCategory.Other -> NetworkShopIssueCategory.Other
    }
}
