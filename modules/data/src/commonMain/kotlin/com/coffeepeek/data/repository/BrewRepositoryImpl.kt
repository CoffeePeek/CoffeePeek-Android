package com.coffeepeek.data.repository

import com.coffeepeek.data.mapper.toDomain
import com.coffeepeek.data.mapper.toEntity
import com.coffeepeek.domain.brew.BrewAdviceEngine
import com.coffeepeek.domain.brew.CoffeeOriginCountries
import com.coffeepeek.domain.model.BeanBag
import com.coffeepeek.domain.model.BrewMethod
import com.coffeepeek.domain.model.BrewSession
import com.coffeepeek.domain.model.BrewSessionDetails
import com.coffeepeek.domain.model.BrewTrends
import com.coffeepeek.domain.model.NewBeanBagInput
import com.coffeepeek.domain.model.NewBrewSessionInput
import com.coffeepeek.domain.model.OriginStat
import com.coffeepeek.domain.model.TasteTag
import com.coffeepeek.domain.repository.BrewRepository
import com.coffeepeek.room.DatabaseCore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BrewRepositoryImpl(
    private val database: DatabaseCore,
) : BrewRepository {

    private val beans = database.beanBagDao
    private val sessions = database.brewSessionDao

    override fun observeBeans(): Flow<List<BeanBag>> =
        beans.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeSessions(): Flow<List<BrewSessionDetails>> =
        combine(sessions.observeAll(), beans.observeAll()) { sessionEntities, beanEntities ->
            val beanMap = beanEntities.associateBy { it.id }
            sessionEntities.map { entity ->
                BrewSessionDetails(
                    session = entity.toDomain(),
                    bean = entity.beanId?.let { beanMap[it]?.toDomain() },
                )
            }
        }

    override suspend fun getBeans(): List<BeanBag> =
        beans.getAll().map { it.toDomain() }

    override suspend fun getBean(id: String): BeanBag? =
        beans.getById(id)?.toDomain()

    override suspend fun upsertBean(input: NewBeanBagInput, id: String?): BeanBag {
        val now = System.currentTimeMillis()
        val existing = id?.takeIf { it.isNotBlank() }?.let { beans.getById(it)?.toDomain() }
        val bag = BeanBag(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = input.name.trim(),
            originCountryCode = input.originCountryCode.trim().uppercase(),
            roastLevel = input.roastLevel,
            roasterName = input.roasterName.trim(),
            notes = input.notes.trim(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        beans.upsert(bag.toEntity())
        return bag
    }

    override suspend fun deleteBean(id: String) {
        beans.delete(id)
    }

    override suspend fun getSessions(limit: Int?): List<BrewSessionDetails> {
        val entities = if (limit != null) sessions.getRecent(limit) else sessions.getAll()
        return entities.toDetails()
    }

    override suspend fun getSession(id: String): BrewSessionDetails? {
        val entity = sessions.getById(id) ?: return null
        val bean = entity.beanId?.let { beans.getById(it)?.toDomain() }
        return BrewSessionDetails(session = entity.toDomain(), bean = bean)
    }

    override suspend fun createSession(input: NewBrewSessionInput): BrewSession {
        val now = System.currentTimeMillis()
        val advice = BrewAdviceEngine.advise(input)
        val session = BrewSession(
            id = UUID.randomUUID().toString(),
            beanId = input.beanId?.takeIf { it.isNotBlank() },
            method = input.method,
            doseG = input.doseG,
            yieldOrWaterG = input.yieldOrWaterG,
            durationSec = input.durationSec.coerceAtLeast(0),
            temperatureC = input.temperatureC,
            grindNote = input.grindNote.trim(),
            tasteTags = input.tasteTags.distinct(),
            overallScore = input.overallScore?.coerceIn(1, 5),
            adviceSnapshot = advice,
            notes = input.notes.trim(),
            createdAt = now,
            updatedAt = now,
        )
        sessions.upsert(session.toEntity())
        return session
    }

    override suspend fun deleteSession(id: String) {
        sessions.delete(id)
    }

    override fun previewAdvice(input: NewBrewSessionInput): String =
        BrewAdviceEngine.advise(input)

    override suspend fun getTrends(periodDays: Int): BrewTrends {
        val days = periodDays.coerceAtLeast(1)
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val from = now - days * dayMs
        val previousFrom = from - days * dayMs
        val current = sessions.getSince(from).map { it.toDomain() }
        val previous = sessions.getSince(previousFrom)
            .map { it.toDomain() }
            .filter { it.createdAt < from }

        val methodCounts = current.groupingBy { it.method }.eachCount()
        val tasteCounts = current.flatMap { it.tasteTags }.groupingBy { it }.eachCount()
        val avg = current.mapNotNull { it.overallScore }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        val prevAvg = previous.mapNotNull { it.overallScore }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        val delta = if (avg != null && prevAvg != null) avg - prevAvg else null

        val shift = buildTasteShift(current, previous)

        return BrewTrends(
            periodDays = days,
            sessionCount = current.size,
            averageScore = avg,
            methodCounts = methodCounts,
            tasteCounts = tasteCounts,
            scoreDeltaVsPrevious = delta,
            dominantTasteShift = shift,
        )
    }

    override suspend fun getOriginStats(): List<OriginStat> {
        val beanList = beans.getAll().map { it.toDomain() }
        val sessionList = sessions.getAll()
        val sessionsByBean = sessionList.groupingBy { it.beanId }.eachCount()
        return beanList
            .groupBy { it.originCountryCode.uppercase() }
            .map { (code, bags) ->
                val sessionCount = bags.sumOf { sessionsByBean[it.id] ?: 0 }
                OriginStat(
                    countryCode = code,
                    countryNameRu = CoffeeOriginCountries.nameRu(code),
                    beanCount = bags.size,
                    sessionCount = sessionCount,
                )
            }
            .sortedWith(
                compareByDescending<OriginStat> { it.sessionCount }
                    .thenByDescending { it.beanCount }
                    .thenBy { it.countryNameRu },
            )
    }

    override suspend fun getSessionsByOrigin(countryCode: String): List<BrewSessionDetails> =
        sessions.getByOrigin(countryCode).toDetails()

    private suspend fun List<com.coffeepeek.room.entity.BrewSessionEntity>.toDetails(): List<BrewSessionDetails> {
        val beanMap = beans.getAll().associateBy { it.id }
        return map { entity ->
            BrewSessionDetails(
                session = entity.toDomain(),
                bean = entity.beanId?.let { beanMap[it]?.toDomain() },
            )
        }
    }

    private fun buildTasteShift(
        current: List<BrewSession>,
        previous: List<BrewSession>,
    ): String? {
        if (current.isEmpty()) return null
        fun share(list: List<BrewSession>, tag: TasteTag): Float {
            if (list.isEmpty()) return 0f
            val hits = list.count { tag in it.tasteTags }
            return hits.toFloat() / list.size
        }
        val sourDelta = share(current, TasteTag.SOUR) - share(previous, TasteTag.SOUR)
        val bitterDelta = share(current, TasteTag.BITTER) - share(previous, TasteTag.BITTER)
        return when {
            sourDelta >= 0.15f && sourDelta >= bitterDelta -> "Стало кислее"
            bitterDelta >= 0.15f && bitterDelta > sourDelta -> "Стало горчее"
            share(current, TasteTag.BALANCED) >= 0.4f -> "Чаще сбалансировано"
            else -> null
        }
    }
}
