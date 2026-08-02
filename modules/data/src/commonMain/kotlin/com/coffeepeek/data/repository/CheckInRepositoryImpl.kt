package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateCheckInReq
import com.coffeepeek.api.model.response.shop.RatingDto
import com.coffeepeek.api.service.CheckInApiService
import com.coffeepeek.domain.model.CheckIn
import com.coffeepeek.domain.model.CreateCheckInInput
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.repository.CheckInRepository

class CheckInRepositoryImpl(
    private val checkInApiService: CheckInApiService,
) : CheckInRepository {

    override suspend fun createCheckIn(input: CreateCheckInInput): Result<Unit> = runCatching {
        val placeRating = input.placeRating
        val serviceRating = input.serviceRating
        val coffeeRating = input.coffeeRating
        val rating = if (placeRating != null && serviceRating != null && coffeeRating != null) {
            RatingDto(
                place = placeRating,
                service = serviceRating,
                coffee = coffeeRating,
            )
        } else {
            null
        }

        checkInApiService.createCheckIn(
            CreateCheckInReq(
                coffeeShopId = input.shopId,
                isPublic = input.isPublic,
                visitedAt = input.visitedAtIso,
                note = input.note?.takeIf { it.isNotBlank() },
                rating = rating,
            )
        ).getOrThrow()
    }

    override suspend fun getMyCheckIns(page: Int, pageSize: Int): Result<PagedResult<CheckIn>> =
        checkInApiService.getMyCheckIns(page, pageSize).map { response ->
            PagedResult(
                items = response.checkIns.map { dto ->
                    CheckIn(
                        id = dto.id,
                        shopId = dto.shopId,
                        shopName = dto.shopName,
                        note = dto.note,
                        createdAt = dto.createdAt,
                        reviewId = dto.reviewId,
                    )
                },
                totalCount = response.totalItems,
                totalPages = response.totalPages,
                currentPage = response.currentPage,
            )
        }
}
