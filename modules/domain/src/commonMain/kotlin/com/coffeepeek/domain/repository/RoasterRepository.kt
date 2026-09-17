package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CreateRoasterInput
import com.coffeepeek.domain.model.RoasterDetails
import com.coffeepeek.domain.model.RoasterSubmissionResult

interface RoasterRepository {
    suspend fun getRoaster(id: String): Result<RoasterDetails>
    suspend fun submitRoaster(input: CreateRoasterInput): Result<RoasterSubmissionResult>
}
