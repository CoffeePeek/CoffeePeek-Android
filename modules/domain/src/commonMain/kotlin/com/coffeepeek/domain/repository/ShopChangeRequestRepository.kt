package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.ModerationStatus
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.model.ShopChangeDraft
import com.coffeepeek.domain.model.ShopChangeRequest
import com.coffeepeek.domain.model.ShopChangeSection

interface ShopChangeRequestRepository {
    suspend fun create(draft: ShopChangeDraft): Result<ShopChangeRequest>
    suspend fun getMine(
        page: Int,
        pageSize: Int,
        status: ModerationStatus? = null,
        shopId: String? = null,
        section: ShopChangeSection? = null,
    ): Result<PagedResult<ShopChangeRequest>>
    suspend fun getById(id: String): Result<ShopChangeRequest>
    suspend fun update(id: String, draft: ShopChangeDraft): Result<ShopChangeRequest>
}
