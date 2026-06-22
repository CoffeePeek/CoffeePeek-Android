package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateCheckInReq
import com.coffeepeek.api.model.request.SendReviewReq
import com.coffeepeek.api.model.request.UpdateReviewReq
import com.coffeepeek.api.model.response.shop.RatingDto
import com.coffeepeek.api.service.CheckInApiService
import com.coffeepeek.api.service.ReviewApiService
import com.coffeepeek.data.mapper.ShopMapper.toDomain
import com.coffeepeek.domain.model.CreateCheckInInput
import com.coffeepeek.domain.model.CreateReviewInput
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.UpdateReviewInput
import com.coffeepeek.domain.repository.CheckInRepository
import com.coffeepeek.domain.repository.PhotoRepository
import com.coffeepeek.domain.repository.ReviewRepository

class ReviewRepositoryImpl(
    private val reviewApiService: ReviewApiService,
    private val photoRepository: PhotoRepository,
) : ReviewRepository {

    override suspend fun canCreateReview(shopId: String): Result<Pair<Boolean, String?>> =
        reviewApiService.canCreateReview(shopId).map { it.canCreate to it.reviewId }

    override suspend fun createReview(input: CreateReviewInput): Result<String> = runCatching {
        val photos = photoRepository.uploadShopPhotos(input.photos).getOrThrow()
        reviewApiService.createReview(
            SendReviewReq(
                shopId = input.shopId,
                header = input.header,
                comment = input.comment,
                rating = RatingDto(
                    place = input.placeRating,
                    service = input.serviceRating,
                    coffee = input.coffeeRating,
                ),
                photos = photos.toUploadedPhotoReqs().takeIf { it.isNotEmpty() },
            )
        ).getOrThrow().entityId ?: error("Не удалось создать отзыв")
    }

    override suspend fun updateReview(reviewId: String, input: UpdateReviewInput): Result<Unit> = runCatching {
        val photos = photoRepository.uploadShopPhotos(input.photos).getOrThrow()
        reviewApiService.updateReview(
            reviewId = reviewId,
            req = UpdateReviewReq(
                header = input.header,
                comment = input.comment,
                rating = RatingDto(
                    place = input.placeRating,
                    service = input.serviceRating,
                    coffee = input.coffeeRating,
                ),
                photos = photos.toUploadedPhotoReqs().takeIf { it.isNotEmpty() },
            )
        ).getOrThrow()
    }

    override suspend fun getUserReviews(
        userId: String,
        page: Int,
        pageSize: Int,
    ): Result<PagedResult<Review>> =
        reviewApiService.getUserReviews(userId, page, pageSize).map { response ->
            PagedResult(
                items = response.reviewDtos.map { it.toDomain() },
                totalCount = response.totalItems,
                totalPages = response.totalPages,
                currentPage = response.currentPage,
            )
        }
}

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

    override suspend fun getMyCheckIns(page: Int, pageSize: Int): Result<PagedResult<com.coffeepeek.domain.model.CheckIn>> =
        checkInApiService.getMyCheckIns(page, pageSize).map { response ->
            PagedResult(
                items = response.checkIns.map { dto ->
                    com.coffeepeek.domain.model.CheckIn(
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
