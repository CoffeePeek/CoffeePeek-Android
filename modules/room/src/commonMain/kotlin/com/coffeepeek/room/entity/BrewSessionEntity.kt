package com.coffeepeek.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "brew_session",
    foreignKeys = [
        ForeignKey(
            entity = BeanBagEntity::class,
            parentColumns = ["id"],
            childColumns = ["bean_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("bean_id"),
        Index("created_at"),
        Index("method"),
    ],
)
data class BrewSessionEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("bean_id")
    val beanId: String?,
    @ColumnInfo("method")
    val method: String,
    @ColumnInfo("dose_g")
    val doseG: Float,
    @ColumnInfo("yield_or_water_g")
    val yieldOrWaterG: Float,
    @ColumnInfo("duration_sec")
    val durationSec: Int,
    @ColumnInfo("temperature_c")
    val temperatureC: Float?,
    @ColumnInfo("grind_note")
    val grindNote: String,
    @ColumnInfo("taste_tags")
    val tasteTags: String,
    @ColumnInfo("overall_score")
    val overallScore: Int?,
    @ColumnInfo("advice_snapshot")
    val adviceSnapshot: String,
    @ColumnInfo("notes")
    val notes: String,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
)
