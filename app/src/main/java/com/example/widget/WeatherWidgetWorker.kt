package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.ForecastResponseDto
import com.example.data.api.OpenMeteoApi
import com.example.data.local.CityPreferences
import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.City
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WeatherWidgetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DannyWeatherWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return Result.success()

        val activeCity = CityPreferences.getSelectedCity(context)
        val tempUnit = CityPreferences.getTempUnit(context)
        val language = CityPreferences.resolveInitialAppLanguage(context)
        val db = WeatherDatabase.getInstance(context)

        val dto = fetchOrLoadCachedForecast(activeCity, db)

        appWidgetIds.forEach { appWidgetId ->
            val views = DannyWeatherWidgetProvider.buildDataRemoteViews(
                context = context,
                city = activeCity,
                tempUnit = tempUnit,
                dto = dto,
                language = language
            )
            DannyWeatherWidgetProvider.attachClickPendingIntents(context, views, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        return if (dto != null) Result.success() else Result.retry()
    }

    private suspend fun fetchOrLoadCachedForecast(
        city: City,
        db: WeatherDatabase
    ): ForecastResponseDto? {
        return try {
            val liveDto = OpenMeteoApi.create().getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )
            try {
                db.weatherDao().insertCache(
                    WeatherCacheEntity(
                        cityId = city.id,
                        cityName = city.nameUk,
                        cachedForecastJson = forecastAdapter.toJson(liveDto),
                        cachedAqiJson = null,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
                // Non-fatal cache write failure
            }
            liveDto
        } catch (_: Exception) {
            try {
                val cached = db.weatherDao().getCachedWeather(city.id)
                cached?.let { forecastAdapter.fromJson(it.cachedForecastJson) }
            } catch (_: Exception) {
                null
            }
        }
    }

    private companion object {
        val moshi: Moshi by lazy {
            Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        }
        val forecastAdapter by lazy {
            moshi.adapter(ForecastResponseDto::class.java)
        }
    }
}
