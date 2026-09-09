package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateRoasterSubmissionReq
import com.coffeepeek.api.service.RoasterApiService
import com.coffeepeek.data.util.FileUrlResolver
import com.coffeepeek.domain.model.CreateRoasterInput
import com.coffeepeek.domain.model.RoasterContact
import com.coffeepeek.domain.model.RoasterDetails
import com.coffeepeek.domain.model.RoasterLocation
import com.coffeepeek.domain.model.RoasterPhoto
import com.coffeepeek.domain.model.RoasterShop
import com.coffeepeek.domain.model.RoasterSubmissionResult
import com.coffeepeek.domain.repository.PhotoRepository
import com.coffeepeek.domain.repository.RoasterRepository

class RoasterRepositoryImpl(
    private val roasterApiService: RoasterApiService,
    private val photoRepository: PhotoRepository,
    private val fileUrlResolver: FileUrlResolver,
) : RoasterRepository {

    override suspend fun getRoaster(id: String): Result<RoasterDetails> =
        roasterApiService.getRoaster(id).map { dto ->
            RoasterDetails(
                id = dto.id,
                name = dto.name,
                about = dto.about,
                location = dto.location
                    ?.takeIf { it.address.isNotBlank() }
                    ?.let { RoasterLocation(it.address, it.latitude, it.longitude) },
                contact = dto.contact?.let {
                    RoasterContact(
                        instagramLink = it.instagramLink?.takeIf(String::isNotBlank),
                        siteLink = it.siteLink?.takeIf(String::isNotBlank),
                    )
                },
                photos = dto.photos
                    .sortedBy { it.sortIndex }
                    .mapNotNull { photo ->
                        val url = fileUrlResolver.resolve(photo.storageKey, photo.fullUrl)
                            ?: return@mapNotNull null
                        RoasterPhoto(
                            id = photo.id,
                            fileName = photo.fileName,
                            storageKey = photo.storageKey,
                            fullUrl = url,
                            sortIndex = photo.sortIndex,
                        )
                    },
                shops = dto.shops.map { RoasterShop(it.id, it.name) },
            )
        }

    override suspend fun submitRoaster(
        input: CreateRoasterInput,
    ): Result<RoasterSubmissionResult> = runCatching {
        val uploadedPhotos = photoRepository.uploadRoasterPhotos(input.photos).getOrThrow()
        val result = roasterApiService.submitRoaster(
            CreateRoasterSubmissionReq(
                name = input.name.trim(),
                about = input.about?.trim()?.takeIf { it.isNotBlank() },
                cityId = input.cityId,
                address = input.address?.trim()?.takeIf { it.isNotBlank() },
                instagramLink = input.instagramLink?.trim()?.takeIf { it.isNotBlank() },
                siteLink = input.siteLink?.trim()?.takeIf { it.isNotBlank() },
                photos = uploadedPhotos.toUploadedPhotoReqs().takeIf { it.isNotEmpty() },
            ),
        ).getOrThrow()
        RoasterSubmissionResult(
            roasterId = result.data.roasterId,
            status = result.data.status,
            isAddressValidated = result.data.isAddressValidated,
            message = result.message,
        )
    }
}
