package com.coffeepeek.domain.repository

import com.coffeepeek.domain.model.BeanBag
import com.coffeepeek.domain.model.BrewSession
import com.coffeepeek.domain.model.BrewSessionDetails
import com.coffeepeek.domain.model.BrewTrends
import com.coffeepeek.domain.model.NewBeanBagInput
import com.coffeepeek.domain.model.NewBrewSessionInput
import com.coffeepeek.domain.model.OriginStat
import kotlinx.coroutines.flow.Flow

interface BrewRepository {
    fun observeBeans(): Flow<List<BeanBag>>
    fun observeSessions(): Flow<List<BrewSessionDetails>>

    suspend fun getBeans(): List<BeanBag>
    suspend fun getBean(id: String): BeanBag?
    suspend fun upsertBean(input: NewBeanBagInput, id: String? = null): BeanBag
    suspend fun deleteBean(id: String)

    suspend fun getSessions(limit: Int? = null): List<BrewSessionDetails>
    suspend fun getSession(id: String): BrewSessionDetails?
    suspend fun createSession(input: NewBrewSessionInput): BrewSession
    suspend fun deleteSession(id: String)

    /** Builds advice without persisting. */
    fun previewAdvice(input: NewBrewSessionInput): String

    suspend fun getTrends(periodDays: Int): BrewTrends
    suspend fun getOriginStats(): List<OriginStat>
    suspend fun getSessionsByOrigin(countryCode: String): List<BrewSessionDetails>
}
