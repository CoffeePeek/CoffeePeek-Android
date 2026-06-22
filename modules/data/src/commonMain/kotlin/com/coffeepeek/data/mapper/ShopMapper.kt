package com.coffeepeek.data.mapper

import com.coffeepeek.api.model.response.shop.CoffeeShopDetailsDto
import com.coffeepeek.api.model.response.shop.ReviewDto
import com.coffeepeek.api.model.response.shop.ShortShopDto
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.ReviewRating

internal object ShopMapper {

    fun ShortShopDto.toDomain() = CoffeeShop(
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

    fun CoffeeShopDetailsDto.toDomain() = CoffeeShopDetails(
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
        cityId = cityId,
        description = description,
        location = location?.let {
            com.coffeepeek.domain.model.ShopLocation(
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
            )
        },
        isVisited = isVisited,
        isNew = isNew,
        canCreateReview = canCreateReview,
        existingReviewId = existingReviewId,
        photos = photos.mapNotNull { it.fullUrl },
        reviews = reviews.map { it.toDomain() },
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
        schedules = schedules.orEmpty().map { schedule ->
            com.coffeepeek.domain.model.ShopSchedule(
                dayOfWeek = schedule.dayOfWeek,
                isClosed = schedule.isClosed,
                intervals = schedule.intervals.orEmpty().map { interval ->
                    com.coffeepeek.domain.model.ScheduleInterval(
                        openTime = interval.openTime,
                        closeTime = interval.closeTime,
                    )
                },
            )
        },
    )

    fun ReviewDto.toDomain() = Review(
        id = id,
        shopId = coffeeShopId,
        username = username,
        header = header,
        comment = comment,
        rating = ReviewRating(
            place = rating.place,
            service = rating.service,
            coffee = rating.coffee,
        ),
        createdAt = createdAtUtc,
        photoUrls = photos.mapNotNull { photo ->
            photo.storageKey.takeIf { it.isNotBlank() }?.let { key ->
                "https://bucket-dev-771f.up.railway.app/coffee.shops/$key"
            }
        },
    )

    private fun priceRangeLabel(range: Int) = when (range) {
        1 -> "$"
        2 -> "$$"
        3 -> "$$$"
        4 -> "$$$$"
        else -> null
    }
}
