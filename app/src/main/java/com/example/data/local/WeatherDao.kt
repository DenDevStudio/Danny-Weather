package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId LIMIT 1")
    suspend fun getCachedWeather(cityId: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cityId = :cityId")
    suspend fun deleteCache(cityId: String)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllCache()
}
