package com.coffeepeek.data.mapper

import com.coffeepeek.domain.model.BeanBag
import com.coffeepeek.domain.model.BrewMethod
import com.coffeepeek.domain.model.BrewSession
import com.coffeepeek.domain.model.RoastLevel
import com.coffeepeek.domain.model.TasteTag
import com.coffeepeek.room.entity.BeanBagEntity
import com.coffeepeek.room.entity.BrewSessionEntity

fun BeanBagEntity.toDomain(): BeanBag = BeanBag(
    id = id,
    name = name,
    originCountryCode = originCountryCode,
    roastLevel = RoastLevel.fromCode(roastLevel),
    roasterName = roasterName,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun BeanBag.toEntity(): BeanBagEntity = BeanBagEntity(
    id = id,
    name = name,
    originCountryCode = originCountryCode,
    roastLevel = roastLevel.code,
    roasterName = roasterName,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun BrewSessionEntity.toDomain(): BrewSession = BrewSession(
    id = id,
    beanId = beanId,
    method = BrewMethod.fromCode(method),
    doseG = doseG,
    yieldOrWaterG = yieldOrWaterG,
    durationSec = durationSec,
    temperatureC = temperatureC,
    grindNote = grindNote,
    tasteTags = TasteTag.parseList(tasteTags),
    overallScore = overallScore,
    adviceSnapshot = adviceSnapshot,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun BrewSession.toEntity(): BrewSessionEntity = BrewSessionEntity(
    id = id,
    beanId = beanId,
    method = method.code,
    doseG = doseG,
    yieldOrWaterG = yieldOrWaterG,
    durationSec = durationSec,
    temperatureC = temperatureC,
    grindNote = grindNote,
    tasteTags = TasteTag.encodeList(tasteTags),
    overallScore = overallScore,
    adviceSnapshot = adviceSnapshot,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
