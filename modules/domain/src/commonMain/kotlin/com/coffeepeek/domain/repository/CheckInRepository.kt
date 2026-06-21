package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CheckIn
import com.coffeepeek.domain.model.CreateCheckInInput
import com.coffeepeek.domain.model.PagedResult

interface CheckInRepository {
    suspend fun createCheckIn(input: CreateCheckInInput): Result<Unit>
    suspend fun getMyCheckIns(page: Int, pageSize: Int): Result<PagedResult<CheckIn>>
}
