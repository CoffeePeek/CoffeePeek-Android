package com.coffeepeek.data.mapper

import com.coffeepeek.api.model.response.shop.CoffeeShopDetailsDto
import com.coffeepeek.api.model.response.shop.ReviewDto
import com.coffeepeek.api.model.response.shop.ShopMenuDto
import com.coffeepeek.api.model.response.shop.ShopMenuItemDto
import com.coffeepeek.api.model.response.shop.ShortShopDto
import com.coffeepeek.data.util.FileUrlResolver
import com.coffeepeek.domain.model.CoffeeShop
import com.coffeepeek.domain.model.CoffeeShopDetails
import com.coffeepeek.domain.model.CoffeeShopType
import com.coffeepeek.domain.model.Review
import com.coffeepeek.domain.model.ReviewRating
import com.coffeepeek.domain.model.ShopMenu
import com.coffeepeek.domain.model.ShopMenuItem
import com.coffeepeek.domain.model.ShopMenuPhoto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

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
        isNew = isNew,
        isVisited = isVisited,
        reviewCount = reviewCount,
        tags = extractBackendTags(tags, shopTags)
            .ifEmpty { (brewMethods + roasters + beans).map { it.name } }
            .take(3),
        type = parseShopType(type, coffeeFocus),
    )

    fun CoffeeShopDetailsDto.toDomain(fileUrls: FileUrlResolver) = CoffeeShopDetails(
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
            isNew = isNew,
            isVisited = isVisited,
            reviewCount = reviewCount,
            tags = extractBackendTags(tags, shopTags)
                .ifEmpty { (brewMethods + roasters + coffeeBeans).map { it.name } }
                .take(3),
            type = parseShopType(type, coffeeFocus),
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
        reviews = reviews.map { it.toDomain(fileUrls) },
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
                dayOfWeek = parseDayOfWeek(schedule.dayOfWeek),
                isClosed = schedule.isClosed,
                intervals = schedule.intervals.orEmpty().map { interval ->
                    com.coffeepeek.domain.model.ScheduleInterval(
                        openTime = interval.openTime,
                        closeTime = interval.closeTime,
                    )
                },
            )
        },
        menu = menu?.toDomain(),
    )

    fun ReviewDto.toDomain(fileUrls: FileUrlResolver) = Review(
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
            fileUrls.resolve(photo.storageKey, photo.fullUrl)
        },
    )

    fun ShopMenuDto.toDomain() = ShopMenu(
        capturedAtUtc = capturedAtUtc,
        updatedAtUtc = updatedAtUtc,
        currency = currency.ifBlank { "BYN" },
        items = items.map { it.toDomain() },
        photos = photos
            .mapNotNull { photo ->
                val url = photo.fullUrl?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                ShopMenuPhoto(
                    id = photo.id,
                    fullUrl = url,
                    sortIndex = photo.sortIndex,
                )
            }
            .sortedBy { it.sortIndex },
    )

    private fun ShopMenuItemDto.toDomain() = ShopMenuItem(
        slug = slug,
        nameRu = nameRu,
        nameEn = nameEn,
        category = category,
        availability = availability,
        price = price.toDoubleOrNull(),
        currency = currency.ifBlank { "BYN" },
    )

    fun parseShopType(type: JsonElement?, coffeeFocus: JsonElement? = null): String {
        val raw = type.takeUnless { it == null || it is JsonNull } ?: coffeeFocus
        return CoffeeShopType.fromApi((raw as? JsonPrimitive)?.contentOrNull)
    }

    private fun JsonElement?.toDoubleOrNull(): Double? {
        val primitive = this as? JsonPrimitive ?: return null
        primitive.doubleOrNull?.let { return it }
        return primitive.contentOrNull
            ?.trim()
            ?.replace(',', '.')
            ?.toDoubleOrNull()
    }

    private fun priceRangeLabel(range: JsonElement?): String? {
        val value = (range as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()
        if (value.isBlank()) return null
        return when (value.lowercase()) {
            "1", "cheap", "$" -> "$"
            "2", "moderate", "$$" -> "$$"
            "3", "expensive", "$$$" -> "$$$"
            "4", "luxury", "$$$$" -> "$$$$"
            else -> null
        }
    }

    private fun parseDayOfWeek(raw: JsonElement?): Int {
        val token = (raw as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()
        if (token.isBlank()) return 0
        return when (token.lowercase()) {
            "0", "sunday" -> 0
            "1", "monday" -> 1
            "2", "tuesday" -> 2
            "3", "wednesday" -> 3
            "4", "thursday" -> 4
            "5", "friday" -> 5
            "6", "saturday" -> 6
            else -> token.toIntOrNull()?.coerceIn(0, 6) ?: 0
        }
    }

    private fun extractBackendTags(vararg rawCandidates: JsonElement?): List<String> {
        val parsed = rawCandidates
            .asSequence()
            .flatMap { parseTagNames(it).asSequence() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
        return parsed.distinct()
    }

    private fun parseTagNames(raw: JsonElement?): List<String> {
        val array = raw as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            when (item) {
                is JsonPrimitive -> item.contentOrNull
                is JsonObject -> item.readTagName()
                else -> null
            }
        }
    }

    private fun JsonObject.readTagName(): String? {
        val keys = listOf("name", "title", "label", "value")
        return keys.asSequence()
            .mapNotNull { key ->
                (this[key] as? JsonPrimitive)?.contentOrNull
            }
            .firstOrNull { it.isNotBlank() }
    }
}
