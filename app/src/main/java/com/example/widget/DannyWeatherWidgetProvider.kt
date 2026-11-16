package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.MainActivity
import com.example.R
import com.example.data.api.ForecastResponseDto
import com.example.data.local.CityPreferences
import com.example.data.local.WeatherDatabase
import com.example.data.model.City
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherCodeMapper
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.localizedCountry
import com.example.ui.i18n.localizedName
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class DannyWeatherWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val appContext = context.applicationContext
        schedulePeriodicWork(appContext)
        enqueueImmediateSync(appContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        try {
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(PERIODIC_WORK_NAME)
        } catch (_: Exception) {
            // Non-fatal in test or restricted environments
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                pushCachedWidgetsOffMainThread(appContext, appWidgetManager, appWidgetIds)
                schedulePeriodicWork(appContext)
                enqueueImmediateSync(appContext)
            } finally {
                try {
                    pendingResult.finish()
                } catch (_: Exception) {
                    // Ignore if already finished
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == ACTION_MANUAL_REFRESH || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val appContext = context.applicationContext
            val pendingResult = goAsync()
            widgetScope.launch {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(appContext)
                    val componentName = ComponentName(appContext, DannyWeatherWidgetProvider::class.java)
                    val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                        ?.takeIf { it.isNotEmpty() }
                        ?: appWidgetManager.getAppWidgetIds(componentName)
                    if (ids.isNotEmpty()) {
                        pushCachedWidgetsOffMainThread(appContext, appWidgetManager, ids)
                        schedulePeriodicWork(appContext)
                        enqueueImmediateSync(appContext)
                    }
                } finally {
                    try {
                        pendingResult.finish()
                    } catch (_: Exception) {
                        // Ignore
                    }
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_MANUAL_REFRESH = "com.dendevstudio.dannyweather.ACTION_WIDGET_REFRESH"
        private const val PERIODIC_WORK_NAME = "danny_weather_widget_periodic_sync"
        private const val IMMEDIATE_WORK_NAME = "danny_weather_widget_immediate_sync"
        private const val CACHE_READ_TIMEOUT_MS = 2_500L

        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val moshi: Moshi by lazy {
            Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        }
        private val forecastAdapter by lazy {
            moshi.adapter(ForecastResponseDto::class.java)
        }

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            widgetScope.launch {
                val appWidgetManager = AppWidgetManager.getInstance(appContext)
                val componentName = ComponentName(appContext, DannyWeatherWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds.isEmpty()) return@launch

                pushCachedWidgetsOffMainThread(appContext, appWidgetManager, appWidgetIds)
                schedulePeriodicWork(appContext)
                enqueueImmediateSync(appContext)
            }
        }

        fun schedulePeriodicWork(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicRequest = PeriodicWorkRequestBuilder<WeatherWidgetWorker>(
                    30,
                    TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()

                WorkManager.getInstance(context.applicationContext)
                    .enqueueUniquePeriodicWork(
                        PERIODIC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        periodicRequest
                    )
            } catch (_: Exception) {
                // Non-fatal in Robolectric/test environments
            }
        }

        fun enqueueImmediateSync(context: Context) {
            try {
                val immediateRequest = OneTimeWorkRequestBuilder<WeatherWidgetWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()

                WorkManager.getInstance(context.applicationContext)
                    .enqueueUniqueWork(
                        IMMEDIATE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        immediateRequest
                    )
            } catch (_: Exception) {
                // Non-fatal in Robolectric/test environments
            }
        }

        private suspend fun pushCachedWidgetsOffMainThread(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val activeCity = CityPreferences.getSelectedCity(context)
            val tempUnit = CityPreferences.getTempUnit(context)
            val language = CityPreferences.resolveInitialAppLanguage(context)

            val cachedDto = try {
                withTimeoutOrNull(CACHE_READ_TIMEOUT_MS) {
                    val entity = WeatherDatabase.getInstance(context)
                        .weatherDao()
                        .getCachedWeather(activeCity.id)
                    entity?.let { forecastAdapter.fromJson(it.cachedForecastJson) }
                }
            } catch (_: Exception) {
                null
            }

            appWidgetIds.forEach { appWidgetId ->
                val views = buildDataRemoteViews(
                    context = context,
                    city = activeCity,
                    tempUnit = tempUnit,
                    dto = cachedDto,
                    language = language
                )
                attachClickPendingIntents(context, views, appWidgetId)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun buildDataRemoteViews(
            context: Context,
            city: City,
            tempUnit: TemperatureUnit,
            dto: ForecastResponseDto?,
            language: AppLanguage = CityPreferences.resolveInitialAppLanguage(context)
        ): RemoteViews {
            val cityName = city.localizedName(language)
            val countryBadge = city.localizedCountry(language).ifBlank { "World" }
            val currentDto = dto?.current

            var tempText = "—"
            var condText = tapToRefreshLabel(language)
            var updatedTimeText = "↻ ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))}"

            if (currentDto != null) {
                val tempC = currentDto.temperature2m ?: 0.0
                tempText = formatWidgetTemp(tempC, tempUnit)
                val code = currentDto.weatherCode ?: 0
                val isDay = (currentDto.isDay ?: 1) == 1
                condText = WeatherCodeMapper.localizedConditionName(code, isDay, language)
                val apiTime = currentDto.time?.takeLast(5)
                if (!apiTime.isNullOrBlank()) {
                    updatedTimeText = "↻ $apiTime"
                }
            }

            return RemoteViews(context.packageName, R.layout.widget_danny_weather).apply {
                setTextViewText(R.id.widget_city_name, cityName)
                setTextViewText(R.id.widget_badge, "${city.flagEmoji} $countryBadge")
                setTextViewText(R.id.widget_temperature, tempText)
                setTextViewText(R.id.widget_condition, condText)
                setTextViewText(R.id.widget_updated_time, updatedTimeText)
                setTextViewText(R.id.widget_footer, widgetFooterLabel(language))
                setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_weather)
            }
        }

        fun attachClickPendingIntents(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            val refreshIntent = Intent(context, DannyWeatherWidgetProvider::class.java).apply {
                action = ACTION_MANUAL_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                10_000 + appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_updated_time, refreshPendingIntent)
        }

        private fun tapToRefreshLabel(language: AppLanguage): String = when (language) {
            AppLanguage.UKRAINIAN -> "Натисніть ↻ для оновлення"
            AppLanguage.POLISH -> "Dotknij ↻, aby odświeżyć"
            AppLanguage.GERMAN -> "Tippen ↻ zum Aktualisieren"
            AppLanguage.SPANISH -> "Toca ↻ para actualizar"
            AppLanguage.FRENCH -> "Appuyez sur ↻ pour actualiser"
            AppLanguage.ENGLISH -> "Tap ↻ to refresh"
        }

        private fun widgetFooterLabel(language: AppLanguage): String = when (language) {
            AppLanguage.UKRAINIAN -> "Danny Weather • Відкрити"
            AppLanguage.POLISH -> "Danny Weather • Otwórz"
            AppLanguage.GERMAN -> "Danny Weather • Öffnen"
            AppLanguage.SPANISH -> "Danny Weather • Abrir"
            AppLanguage.FRENCH -> "Danny Weather • Ouvrir"
            AppLanguage.ENGLISH -> "Danny Weather • Open"
        }

        private fun formatWidgetTemp(tempC: Double, unit: TemperatureUnit): String {
            return when (unit) {
                TemperatureUnit.CELSIUS -> {
                    val rounded = tempC.roundToInt()
                    (if (rounded > 0) "+$rounded" else "$rounded") + "°"
                }
                TemperatureUnit.FAHRENHEIT -> {
                    val tempF = (tempC * 9.0 / 5.0) + 32.0
                    "${tempF.roundToInt()}°F"
                }
            }
        }
    }
}
