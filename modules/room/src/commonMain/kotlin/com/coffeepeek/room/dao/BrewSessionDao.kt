package com.coffeepeek.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.coffeepeek.room.entity.BrewSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrewSessionDao {

    @Upsert
    suspend fun upsert(entity: BrewSessionEntity)

    @Query("SELECT * FROM brew_session ORDER BY created_at DESC")
    fun observeAll(): Flow<List<BrewSessionEntity>>

    @Query("SELECT * FROM brew_session ORDER BY created_at DESC")
    suspend fun getAll(): List<BrewSessionEntity>

    @Query("SELECT * FROM brew_session ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<BrewSessionEntity>

    @Query("SELECT * FROM brew_session WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BrewSessionEntity?

    @Query("SELECT * FROM brew_session WHERE created_at >= :fromMs ORDER BY created_at DESC")
    suspend fun getSince(fromMs: Long): List<BrewSessionEntity>

    @Query(
        """
        SELECT s.* FROM brew_session s
        LEFT JOIN bean_bag b ON b.id = s.bean_id
        WHERE UPPER(b.origin_country_code) = UPPER(:countryCode)
        ORDER BY s.created_at DESC
        """,
    )
    suspend fun getByOrigin(countryCode: String): List<BrewSessionEntity>

    @Query("DELETE FROM brew_session WHERE id = :id")
    suspend fun delete(id: String)
}
