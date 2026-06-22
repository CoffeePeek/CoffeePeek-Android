package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateShopContactReq
import com.coffeepeek.api.model.request.CreateShopReq
import com.coffeepeek.api.model.request.ScheduleIntervalReq
import com.coffeepeek.api.model.request.ScheduleReq
import com.coffeepeek.api.service.ShopApiService
import com.coffeepeek.data.mapper.ShopMapper.toDomain
import com.coffeepeek.domain.model.CatalogItem
import com.coffeepeek.domain.model.City
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.CreateShopInput
import com.coffeepeek.domain.model.MapBounds
import com.coffeepeek.domain.model.MapShop
import com.coffeepeek.domain.model.PagedResult
import com.coffeepeek.domain.model.ShopCatalogs
import com.coffeepeek.domain.model.ShopFilters
import com.coffeepeek.domain.repository.PhotoRepository
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ShopRepositoryImpl(
    private val shopApiService: ShopApiService,
    private val photoRepository: PhotoRepository,
) : ShopRepository {

    private var cachedCatalogs: ShopCatalogs? = null

    override suspend fun searchShops(filters: ShopFilters): Result<PagedResult<CoffeeShop>> =
        shopApiService.searchShops(
            query = filters.query,
            cityId = filters.cityId,
            roasterIds = filters.roasterIds.takeIf { it.isNotEmpty() },
            equipmentIds = filters.equipmentIds.takeIf { it.isNotEmpty() },
            beanIds = filters.beanIds.takeIf { it.isNotEmpty() },
            brewMethodIds = filters.brewMethodIds.takeIf { it.isNotEmpty() },
            priceRange = filters.priceRange,
            minRating = filters.minRating,
            page = filters.page,
            pageSize = filters.pageSize,
        ).map { dto ->
            PagedResult(
                items = dto.coffeeShops.map { it.toDomain() },
                totalCount = dto.totalItems,
                totalPages = dto.totalPages,
                currentPage = dto.currentPage,
            )
        }

    override suspend fun getShopDetails(id: String): Result<CoffeeShopDetails> =
        shopApiService.getShopDetails(id).map { it.toDomain() }

    override suspend fun getShopsInBounds(bounds: MapBounds, filters: ShopFilters): Result<List<MapShop>> =
        shopApiService.getShopsInBounds(
            minLat = bounds.minLat,
            minLon = bounds.minLon,
            maxLat = bounds.maxLat,
            maxLon = bounds.maxLon,
            query = filters.query,
            cityId = filters.cityId,
            roasterIds = filters.roasterIds.takeIf { it.isNotEmpty() },
            equipmentIds = filters.equipmentIds.takeIf { it.isNotEmpty() },
            beanIds = filters.beanIds.takeIf { it.isNotEmpty() },
            brewMethodIds = filters.brewMethodIds.takeIf { it.isNotEmpty() },
            priceRange = filters.priceRange,
            minRating = filters.minRating,
        ).map { shops ->
            shops.map { dto ->
                MapShop(
                    id = dto.id,
                    title = dto.title,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                )
            }
        }

    override suspend fun getCatalogs(): Result<ShopCatalogs> = cachedCatalogs?.let { Result.success(it) } ?: runCatching {
        coroutineScope {
            val cities      = async { shopApiService.getCities().getOrElse { emptyList() } }
            val beans       = async { shopApiService.getBeans().getOrElse { emptyList() } }
            val equipment   = async { shopApiService.getEquipment().getOrElse { emptyList() } }
            val roasters    = async { shopApiService.getRoasters().getOrElse { emptyList() } }
            val brewMethods = async { shopApiService.getBrewMethods().getOrElse { emptyList() } }
            ShopCatalogs(
                cities      = cities.await().map { City(it.id, it.name) },
                beans       = beans.await().map { CatalogItem(it.id, it.name) },
                equipment   = equipment.await().map { CatalogItem(it.id, it.name) },
                roasters    = roasters.await().map { CatalogItem(it.id, it.name) },
                brewMethods = brewMethods.await().map { CatalogItem(it.id, it.name) },
            ).also { cachedCatalogs = it }
        }
    }

    override suspend fun createShop(input: CreateShopInput): Result<Unit> = runCatching {
        val uploadedPhotos = photoRepository.uploadShopPhotos(input.photos).getOrThrow()

        shopApiService.createShop(
            CreateShopReq(
                name        = input.name,
                address     = input.address,
                cityId      = input.cityId,
                description = input.description?.takeIf { it.isNotBlank() },
                priceRange  = input.priceRange,
                shopContact = if (listOf(input.phone, input.email, input.website, input.instagram).any { !it.isNullOrBlank() }) {
                    CreateShopContactReq(
                        phoneNumber   = input.phone?.takeIf { it.isNotBlank() },
                        email         = input.email?.takeIf { it.isNotBlank() },
                        siteLink      = input.website?.takeIf { it.isNotBlank() },
                        instagramLink = input.instagram?.takeIf { it.isNotBlank() },
                    )
                } else null,
                schedules = input.schedules.takeIf { it.isNotEmpty() }?.map { schedule ->
                    ScheduleReq(
                        dayOfWeek = schedule.dayOfWeek,
                        isClosed = schedule.isClosed,
                        intervals = schedule.intervals.takeIf { it.isNotEmpty() }?.map { interval ->
                            ScheduleIntervalReq(
                                openTime = interval.openTime,
                                closeTime = interval.closeTime,
                            )
                        },
                    )
                },
                shopPhotos = uploadedPhotos.toUploadedPhotoReqs().takeIf { it.isNotEmpty() },
                equipmentIds  = input.equipmentIds.takeIf { it.isNotEmpty() },
                coffeeBeanIds = input.coffeeBeanIds.takeIf { it.isNotEmpty() },
                roasterIds    = input.roasterIds.takeIf { it.isNotEmpty() },
                brewMethodIds = input.brewMethodIds.takeIf { it.isNotEmpty() },
            )
        ).getOrThrow()
    }
}
