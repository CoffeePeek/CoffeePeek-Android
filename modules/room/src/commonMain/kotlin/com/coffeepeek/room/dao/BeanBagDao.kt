package com.coffeepeek.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.coffeepeek.room.entity.BeanBagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeanBagDao {

    @Upsert
    suspend fun upsert(entity: BeanBagEntity)

    @Query("SELECT * FROM bean_bag ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<BeanBagEntity>>

    @Query("SELECT * FROM bean_bag ORDER BY updated_at DESC")
    suspend fun getAll(): List<BeanBagEntity>

    @Query("SELECT * FROM bean_bag WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BeanBagEntity?

    @Query("DELETE FROM bean_bag WHERE id = :id")
    suspend fun delete(id: String)
}
