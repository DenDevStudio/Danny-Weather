package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val cityId: String,
    val cityName: String,
    val cachedForecastJson: String,
    val cachedAqiJson: String?,
    val updatedAtMillis: Long
)
