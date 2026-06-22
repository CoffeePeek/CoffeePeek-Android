package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CreateReviewInput
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.UpdateReviewInput

interface ReviewRepository {
    suspend fun canCreateReview(shopId: String): Result<Pair<Boolean, String?>>
    suspend fun createReview(input: CreateReviewInput): Result<String>
    suspend fun updateReview(reviewId: String, input: UpdateReviewInput): Result<Unit>
    suspend fun getUserReviews(userId: String, page: Int, pageSize: Int): Result<PagedResult<Review>>
}
