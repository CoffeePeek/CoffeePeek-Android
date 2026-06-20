package com.coffeepeek.data.repository

import com.coffeepeek.api.model.request.CreateShopContactReq
import com.coffeepeek.api.model.request.CreateShopReq
import com.coffeepeek.api.model.response.shop.CoffeeShopDetailsDto
import com.coffeepeek.api.model.response.shop.ShortShopDto
import com.coffeepeek.api.service.ShopApiService
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
import com.coffeepeek.domain.repository.ShopRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ShopRepositoryImpl(
    private val shopApiService: ShopApiService,
) : ShopRepository {

    override suspend fun searchShops(filters: ShopFilters): Result<PagedResult<CoffeeShop>> =
        shopApiService.searchShops(
            query = filters.query,
            cityId = filters.cityId,
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

    override suspend fun getShopsInBounds(bounds: MapBounds): Result<List<MapShop>> =
        shopApiService.getShopsInBounds(
            minLat = bounds.minLat,
            minLon = bounds.minLon,
            maxLat = bounds.maxLat,
            maxLon = bounds.maxLon,
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

    override suspend fun getCatalogs(): Result<ShopCatalogs> = runCatching {
        coroutineScope {
            val cities      = async { shopApiService.getCities().getOrDefault(emptyList()) }
            val beans       = async { shopApiService.getBeans().getOrDefault(emptyList()) }
            val equipment   = async { shopApiService.getEquipment().getOrDefault(emptyList()) }
            val roasters    = async { shopApiService.getRoasters().getOrDefault(emptyList()) }
            val brewMethods = async { shopApiService.getBrewMethods().getOrDefault(emptyList()) }
            ShopCatalogs(
                cities      = cities.await().map { City(it.id, it.name) },
                beans       = beans.await().map { CatalogItem(it.id, it.name) },
                equipment   = equipment.await().map { CatalogItem(it.id, it.name) },
                roasters    = roasters.await().map { CatalogItem(it.id, it.name) },
                brewMethods = brewMethods.await().map { CatalogItem(it.id, it.name) },
            )
        }
    }

    override suspend fun createShop(input: CreateShopInput): Result<Unit> =
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
                equipmentIds  = input.equipmentIds.takeIf { it.isNotEmpty() },
                coffeeBeanIds = input.coffeeBeanIds.takeIf { it.isNotEmpty() },
                roasterIds    = input.roasterIds.takeIf { it.isNotEmpty() },
                brewMethodIds = input.brewMethodIds.takeIf { it.isNotEmpty() },
            )
        )

    private fun ShortShopDto.toDomain() = CoffeeShop(
        id = id,
        title = name,
        rating = rating.takeIf { it > 0 },
        cityName = null,
        priceRange = priceRangeLabel(priceRange),
        photoUrl = photos.firstOrNull()?.fullUrl,
        isFavorite = isFavorite,
        address = location?.address,
        isOpen = isOpen,
        reviewCount = reviewCount,
        tags = (brewMethods + roasters + beans).take(3).map { it.name },
    )

    private fun CoffeeShopDetailsDto.toDomain() = CoffeeShopDetails(
        shop = CoffeeShop(
            id = id,
            title = name,
            rating = rating.takeIf { it > 0 },
            cityName = null,
            priceRange = priceRangeLabel(priceRange),
            photoUrl = photos.firstOrNull()?.fullUrl,
            isFavorite = isFavorite,
            address = location?.address,
            isOpen = isOpen,
            reviewCount = reviewCount,
            tags = (brewMethods + roasters + coffeeBeans).take(3).map { it.name },
        ),
        description = description,
        address = location?.address,
        isVisited = isVisited,
        canCreateReview = canCreateReview ?: false,
        photos = photos.mapNotNull { it.fullUrl },
        reviews = reviews.map { review ->
            com.coffeepeek.domain.model.Review(
                id = review.id,
                username = review.username,
                header = review.header,
                comment = review.comment,
                rating = (review.rating.place + review.rating.service + review.rating.coffee) / 3.0,
                createdAt = review.createdAtUtc,
            )
        },
        contact = shopContact?.let { c ->
            com.coffeepeek.domain.model.ShopContact(
                instagram = c.instagramLink,
                email = c.email,
                website = c.siteLink,
                phone = c.phoneNumber,
            )
        },
        brewMethods = brewMethods.map { it.name },
        coffeeBeans = coffeeBeans.map { it.name },
        roasters = roasters.map { it.name },
        equipment = equipments.map { it.name },
    )

    private fun priceRangeLabel(range: Int) = when (range) {
        1 -> "$"
        2 -> "$$"
        3 -> "$$$"
        4 -> "$$$$"
        else -> null
    }
}
