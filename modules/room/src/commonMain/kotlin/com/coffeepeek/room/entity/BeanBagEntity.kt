package com.coffeepeek.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bean_bag")
data class BeanBagEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("origin_country_code")
    val originCountryCode: String,
    @ColumnInfo("roast_level")
    val roastLevel: String,
    @ColumnInfo("roaster_name")
    val roasterName: String,
    @ColumnInfo("notes")
    val notes: String,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
)
