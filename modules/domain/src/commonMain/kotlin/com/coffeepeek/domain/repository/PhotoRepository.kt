package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.PendingPhotoUpload
import com.coffeepeek.domain.model.UploadedPhotoMeta

interface PhotoRepository {
    suspend fun uploadShopPhotos(photos: List<PendingPhotoUpload>): Result<List<UploadedPhotoMeta>>
}
