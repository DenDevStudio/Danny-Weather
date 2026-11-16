package com.example.ui.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.example.data.model.AppAccentTheme
import com.example.data.model.City
import com.example.data.model.WeatherHomeBlock
import com.example.data.model.WorldRegion
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val flagEmoji: String
) {
    UKRAINIAN("uk", "Українська", "🇺🇦"),
    ENGLISH("en", "English", "🇬🇧"),
    POLISH("pl", "Polski", "🇵🇱"),
    GERMAN("de", "Deutsch", "🇩🇪"),
    SPANISH("es", "Español", "🇪🇸"),
    FRENCH("fr", "Français", "🇫🇷");

    val locale: Locale
        get() = Locale.forLanguageTag(code)

    companion object {
        private val SUPPORTED_CODES = setOf("uk", "en", "pl", "de", "es", "fr")

        fun fromLocale(locale: Locale = Locale.getDefault()): AppLanguage {
            val lang = locale.language.lowercase(Locale.ROOT)
            return when (lang) {
                "uk" -> UKRAINIAN
                "pl" -> POLISH
                "de" -> GERMAN
                "es" -> SPANISH
                "fr" -> FRENCH
                "en" -> ENGLISH
                else -> ENGLISH
            }
        }

        fun fromLocaleList(locales: LocaleListCompat): AppLanguage {
            for (i in 0 until locales.size()) {
                val locale = locales[i] ?: continue
                val code = locale.language.lowercase(Locale.ROOT)
                if (code in SUPPORTED_CODES) {
                    return fromLocale(locale)
                }
            }
            return fromLocale(Locale.getDefault())
        }

        fun fromConfiguration(context: Context): AppLanguage {
            val configLocales = ConfigurationCompat.getLocales(context.resources.configuration)
            if (!configLocales.isEmpty) {
                return fromLocaleList(configLocales)
            }
            val defaultLocales = LocaleListCompat.getAdjustedDefault()
            return if (!defaultLocales.isEmpty) {
                fromLocaleList(defaultLocales)
            } else {
                fromLocale(Locale.getDefault())
            }
        }

        fun fromStorage(raw: String?): AppLanguage? {
            if (raw.isNullOrBlank() || raw == SYSTEM_MODE) return null
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.code.equals(raw, ignoreCase = true) }
        }

        val MANUAL_SELECTION_ORDER: List<AppLanguage> by lazy {
            listOf(ENGLISH) + entries
                .filter { it != ENGLISH }
                .sortedBy { it.nativeName.lowercase(Locale.ROOT) }
        }

        const val SYSTEM_MODE = "SYSTEM"
    }
}

fun City.localizedName(language: AppLanguage): String {
    return if (language == AppLanguage.UKRAINIAN) {
        nameUk.ifBlank { nameEn }
    } else {
        nameEn.ifBlank { nameUk }
    }
}

fun City.localizedCountry(language: AppLanguage): String {
    if (language == AppLanguage.UKRAINIAN) {
        return countryUk.ifBlank { regionUk }
    }
    if (countryCode.length == 2) {
        val display = Locale.Builder().setRegion(countryCode).build().getDisplayCountry(language.locale)
        if (display.isNotBlank()) return display
    }
    return countryUk.ifBlank { regionUk }
}

fun City.localizedSubtitle(language: AppLanguage): String {
    return if (language == AppLanguage.UKRAINIAN) {
        regionUk.ifBlank { countryUk }
    } else {
        localizedCountry(language).ifBlank { regionUk }
    }
}

fun WorldRegion.localizedTitle(language: AppLanguage): String {
    return when (language) {
        AppLanguage.UKRAINIAN -> titleUk
        AppLanguage.POLISH -> when (this) {
            WorldRegion.ALL -> "Wszystkie"
            WorldRegion.UKRAINE -> "Ukraina"
            WorldRegion.EUROPE -> "Europa"
            WorldRegion.ASIA -> "Azja"
            WorldRegion.AMERICAS -> "Ameryka"
            WorldRegion.AFRICA -> "Afryka"
            WorldRegion.OCEANIA -> "Oceania"
        }
        AppLanguage.GERMAN -> when (this) {
            WorldRegion.ALL -> "Alle"
            WorldRegion.UKRAINE -> "Ukraine"
            WorldRegion.EUROPE -> "Europa"
            WorldRegion.ASIA -> "Asien"
            WorldRegion.AMERICAS -> "Amerika"
            WorldRegion.AFRICA -> "Afrika"
            WorldRegion.OCEANIA -> "Ozeanien"
        }
        AppLanguage.SPANISH -> when (this) {
            WorldRegion.ALL -> "Todos"
            WorldRegion.UKRAINE -> "Ucrania"
            WorldRegion.EUROPE -> "Europa"
            WorldRegion.ASIA -> "Asia"
            WorldRegion.AMERICAS -> "América"
            WorldRegion.AFRICA -> "África"
            WorldRegion.OCEANIA -> "Oceanía"
        }
        AppLanguage.FRENCH -> when (this) {
            WorldRegion.ALL -> "Tous"
            WorldRegion.UKRAINE -> "Ukraine"
            WorldRegion.EUROPE -> "Europe"
            WorldRegion.ASIA -> "Asie"
            WorldRegion.AMERICAS -> "Amérique"
            WorldRegion.AFRICA -> "Afrique"
            WorldRegion.OCEANIA -> "Océanie"
        }
        AppLanguage.ENGLISH -> when (this) {
            WorldRegion.ALL -> "All countries"
            WorldRegion.UKRAINE -> "Ukraine"
            WorldRegion.EUROPE -> "Europe"
            WorldRegion.ASIA -> "Asia"
            WorldRegion.AMERICAS -> "Americas"
            WorldRegion.AFRICA -> "Africa"
            WorldRegion.OCEANIA -> "Oceania"
        }
    }
}

fun WeatherHomeBlock.localizedTitle(language: AppLanguage): String {
    return when (language) {
        AppLanguage.UKRAINIAN -> titleUk
        AppLanguage.POLISH -> when (this) {
            WeatherHomeBlock.CURRENT -> "Aktualna pogoda"
            WeatherHomeBlock.HOURLY -> "Prognoza godzinowa"
            WeatherHomeBlock.METRICS -> "Szczegóły pogody"
            WeatherHomeBlock.AIR_QUALITY -> "Jakość powietrza (AQI)"
        }
        AppLanguage.GERMAN -> when (this) {
            WeatherHomeBlock.CURRENT -> "Aktuelles Wetter"
            WeatherHomeBlock.HOURLY -> "Stündliche Vorhersage"
            WeatherHomeBlock.METRICS -> "Wetterdetails"
            WeatherHomeBlock.AIR_QUALITY -> "Luftqualität (AQI)"
        }
        AppLanguage.SPANISH -> when (this) {
            WeatherHomeBlock.CURRENT -> "Clima actual"
            WeatherHomeBlock.HOURLY -> "Pronóstico por hora"
            WeatherHomeBlock.METRICS -> "Detalles del clima"
            WeatherHomeBlock.AIR_QUALITY -> "Calidad del aire (AQI)"
        }
        AppLanguage.FRENCH -> when (this) {
            WeatherHomeBlock.CURRENT -> "Météo actuelle"
            WeatherHomeBlock.HOURLY -> "Prévisions horaires"
            WeatherHomeBlock.METRICS -> "Détails météo"
            WeatherHomeBlock.AIR_QUALITY -> "Qualité de l'air (AQI)"
        }
        AppLanguage.ENGLISH -> when (this) {
            WeatherHomeBlock.CURRENT -> "Current Weather"
            WeatherHomeBlock.HOURLY -> "Hourly Forecast"
            WeatherHomeBlock.METRICS -> "Weather Details"
            WeatherHomeBlock.AIR_QUALITY -> "Air Quality (AQI)"
        }
    }
}

data class AppStrings(
    val language: AppLanguage,
    // Tabs & Header
    val tabToday: String,
    val tabSevenDays: String,
    val tabWorldCities: String,
    val tabSettings: String,
    val primaryCityBadge: String,
    val pinActionLabel: String,
    val tapPinHint: String,
    val pinTooltip: String,
    val unpinTooltip: String,
    // Loading & Error
    val loadingWeatherPrefix: String,
    val loadingWeatherSubtitle: String,
    val errorWeatherTitle: String,
    val retryButton: String,
    // Current Weather
    val feelsLikePrefix: String,
    val maxLabel: String,
    val minLabel: String,
    val updatedPrefix: String,
    val refreshWeatherCd: String,
    val changeCityCd: String,
    val locationCd: String,
    // Hourly & Daily
    val hourlyTitle: String,
    val nowLabel: String,
    val precipChanceCd: String,
    val sevenDayForecastPrefix: String,
    val todayLabel: String,
    // Metrics
    val metricWindTitle: String,
    val windDirectionCd: String,
    val metricHumidityTitle: String,
    val humidityHigh: String,
    val humidityDry: String,
    val humidityComfort: String,
    val metricPressureTitle: String,
    val pressureHigh: String,
    val pressureLow: String,
    val pressureNormal: String,
    val metricUvTitle: String,
    val uvLow: String,
    val uvModerate: String,
    val uvHigh: String,
    val uvVeryHigh: String,
    val metricCloudsTitle: String,
    val cloudsClear: String,
    val cloudsPartly: String,
    val cloudsOvercast: String,
    val metricPrecipTitle: String,
    val precipChancePrefix: String,
    val sunriseTitle: String,
    val sunsetTitle: String,
    val mmUnit: String,
    // Air Quality
    val airQualityTitle: String,
    val aqiSubtitle: String,
    val ozoneLabel: String,
    val microgramUnit: String,
    // City Selector & First Launch
    val firstLaunchTitle: String,
    val firstLaunchSubtitle: String,
    val firstLaunchSearchPlaceholder: String,
    val firstLaunchDetectedLangPrefix: String,
    val firstLaunchSkip: String,
    val closeCd: String,
    val clearCd: String,
    val citySheetTitle: String,
    val citySheetSubtitle: String,
    val searchResultsHeader: String,
    val nothingFoundPrefix: String,
    // World Cities
    val worldFindCityTitle: String,
    val worldFindCitySubtitle: String,
    val worldSearchPlaceholder: String,
    val worldResultsHint: String,
    val worldRegionsHint: String,
    val worldEmptyHint: String,
    val aboutCityLabel: String,
    val populationPrefix: String,
    val elevationPrefix: String,
    val metersUnit: String,
    val loadingCityInfoPrefix: String,
    val sourcePrefix: String,
    val showWeatherHere: String,
    val pinnedLabel: String,
    val pinToTopLabel: String,
    // Settings
    val settingsTitle: String,
    val settingsLanguageTitle: String,
    val settingsLanguageSystem: String,
    val settingsTempTitle: String,
    val settingsTempCelsius: String,
    val settingsTempFahrenheit: String,
    val settingsPressureTitle: String,
    val settingsPressureMmhg: String,
    val settingsPressureHpa: String,
    val settingsWindTitle: String,
    val settingsWindMs: String,
    val settingsWindKmh: String,
    val cloudUpdatesTitle: String,
    val cloudConnectedPrefix: String,
    val cloudSignInSubtitle: String,
    val signOutLabel: String,
    val signInWithGoogleLabel: String,
    val aboutDescriptionText: String,
    // Updates
    val updateAvailablePrefix: String,
    val updateDialogSubtitle: String,
    val updateBannerSubtitle: String,
    val downloadPresetLabel: String,
    val downloadShareLabel: String,
    val shareApkLabel: String,
    val openFileLabel: String,
    val laterLabel: String,
    val firstStepCompleted: String,
    val updateNowButton: String,
    val honestForecastDisclaimer: String,
    val proCardTitle: String,
    val proCardActiveVipSubtitle: String,
    val proCardActiveProSubtitle: String,
    val proCardLockedSubtitle: String,
    val proUnlockButtonLabel: String,
    val proLockBannerTitle: String,
    val proLockBannerSubtitle: String,
    val copyrightFooter: String,
    val defaultVersionBanner: String,
    val playStoreOpenError: String,
    val googleConnectedStatus: String,
    val googleSignInSuccessToast: String,
    val googleSignedOutStatus: String,
    val cloudSyncErrorFallback: String,
    val weatherLoadErrorFallback: String,
    val accountUpdateTitle: String,
    val accountUpdateSubtitle: String,
    val accountUpdateBody: String,
    val playMarketLinkLabel: String,
    val openPlayMarketButton: String,
    val cloudAccountQuickLabel: String,
    val cloudAccountConnectedTitle: String,
    val cloudAccountDisconnectedTitle: String,
    val cloudAccountSwitchButton: String,
    val cloudAccountOneTapHint: String
) {
    companion object {
        fun forLanguage(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.UKRAINIAN -> ukrainian()
            AppLanguage.ENGLISH -> english()
            AppLanguage.POLISH -> polish()
            AppLanguage.GERMAN -> german()
            AppLanguage.SPANISH -> spanish()
            AppLanguage.FRENCH -> french()
        }

        private fun ukrainian() = AppStrings(
            language = AppLanguage.UKRAINIAN,
            tabToday = "Сьогодні",
            tabSevenDays = "14 Днів",
            tabWorldCities = "Міста світу",
            tabSettings = "Одиниці",
            primaryCityBadge = "Головне місто",
            pinActionLabel = "Закріпити",
            tapPinHint = "Натисніть 📌, щоб закріпити",
            pinTooltip = "Закріпити",
            unpinTooltip = "Відкріпити",
            loadingWeatherPrefix = "Оновлення погоди:",
            loadingWeatherSubtitle = "Отримуємо дані від метеостанцій",
            errorWeatherTitle = "Не вдалося отримати погоду",
            retryButton = "Спробувати знову",
            feelsLikePrefix = "Відчувається як",
            maxLabel = "Макс",
            minLabel = "Мін",
            updatedPrefix = "Оновлено:",
            refreshWeatherCd = "Оновити погоду",
            changeCityCd = "Змінити місто",
            locationCd = "Місцезнаходження",
            hourlyTitle = "Погодинний прогноз",
            nowLabel = "Зараз",
            precipChanceCd = "Ймовірність опадів",
            sevenDayForecastPrefix = "Прогноз на 14 днів",
            todayLabel = "Сьогодні",
            metricWindTitle = "ВІТЕР",
            windDirectionCd = "Напрямок вітру",
            metricHumidityTitle = "ВОЛОГІСТЬ",
            humidityHigh = "Висока вологість",
            humidityDry = "Сухе повітря",
            humidityComfort = "Комфортно",
            metricPressureTitle = "ТИСК",
            pressureHigh = "Підвищений тиск",
            pressureLow = "Знижений тиск",
            pressureNormal = "Нормальний тиск",
            metricUvTitle = "УФ-ІНДЕКС",
            uvLow = "Низький рівень",
            uvModerate = "Помірний захист",
            uvHigh = "Високий рівень",
            uvVeryHigh = "Дуже високий",
            metricCloudsTitle = "ХМАРНІСТЬ",
            cloudsClear = "Чисте небо",
            cloudsPartly = "Мінлива хмарність",
            cloudsOvercast = "Суцільна хмарність",
            metricPrecipTitle = "ОПАДИ",
            precipChancePrefix = "Ймовірність:",
            sunriseTitle = "СХІД СОНЦЯ",
            sunsetTitle = "ЗАХІД СОНЦЯ",
            mmUnit = "мм",
            airQualityTitle = "Якість повітря (AQI)",
            aqiSubtitle = "Індекс чистоти атмосфери",
            ozoneLabel = "Озон O₃",
            microgramUnit = "мкг/м³",
            firstLaunchTitle = "Оберіть місто або країну",
            firstLaunchSubtitle = "Для якої локації показувати погоду при старті?",
            firstLaunchSearchPlaceholder = "Введіть місто або країну…",
            firstLaunchDetectedLangPrefix = "Мова пристрою:",
            firstLaunchSkip = "Пропустити",
            closeCd = "Закрити",
            clearCd = "Очистити",
            citySheetTitle = "Вибір і закріплення міст",
            citySheetSubtitle = "Оберіть місто або натисніть 📌, щоб закріпити зверху",
            searchResultsHeader = "Результати пошуку у світі",
            nothingFoundPrefix = "Нічого не знайдено за запитом",
            worldFindCityTitle = "Знайти місто та прочитати про нього",
            worldFindCitySubtitle = "Шукайте будь-яке місто світу, читайте довідку та закріплюйте зверху",
            worldSearchPlaceholder = "Введіть своє місто чи країну…",
            worldResultsHint = "Знайдені міста (натисніть, щоб прочитати про місто)",
            worldRegionsHint = "Міста за регіонами (натисніть, щоб прочитати про місто)",
            worldEmptyHint = "Нічого не знайдено. Спробуйте ввести назву міста чи країни.",
            aboutCityLabel = "Про місто",
            populationPrefix = "Населення:",
            elevationPrefix = "Висота:",
            metersUnit = "м",
            loadingCityInfoPrefix = "Завантажуємо інформацію про",
            sourcePrefix = "Джерело:",
            showWeatherHere = "Погода тут",
            pinnedLabel = "Закріплено",
            pinToTopLabel = "Закріпити зверху",
            settingsTitle = "Налаштування",
            settingsLanguageTitle = "Мова додатка",
            settingsLanguageSystem = "Авто",
            settingsTempTitle = "Температура",
            settingsTempCelsius = "°C Цельсій",
            settingsTempFahrenheit = "°F Фаренгейт",
            settingsPressureTitle = "Атмосферний тиск",
            settingsPressureMmhg = "мм рт. ст.",
            settingsPressureHpa = "гПа (hPa)",
            settingsWindTitle = "Швидкість вітру",
            settingsWindMs = "м/с",
            settingsWindKmh = "км/год",
            cloudUpdatesTitle = "Хмарні оновлення",
            cloudConnectedPrefix = "Підключено:",
            cloudSignInSubtitle = "Увійдіть, щоб отримувати нові версії напряму",
            signOutLabel = "Вийти",
            signInWithGoogleLabel = "Увійти через Google",
            aboutDescriptionText = "Погода для всіх міст і країн світу. Ультра-прозорий акриловий інтерфейс Рідке Скло, віджет головного екрана та висока швидкодія.",
            updateAvailablePrefix = "Доступна нова версія",
            updateDialogSubtitle = "Свіжі покращення Danny Weather готові до встановлення",
            updateBannerSubtitle = "Нове оновлення доступне",
            downloadPresetLabel = "Оновити",
            downloadShareLabel = "Оновити",
            shareApkLabel = "Оновити",
            openFileLabel = "Оновити",
            laterLabel = "Пізніше",
            firstStepCompleted = "Перший крок виконано",
            updateNowButton = "Оновити",
            honestForecastDisclaimer = "Стовідсоткової точності на останні 4–5 днів прогнозу не може бути, бо Земля крутиться і погода постійно змінюється",
            proCardTitle = "Danny Weather PRO",
            proCardActiveVipSubtitle = "Активовано через VIP-доступ • 14 днів відкрито",
            proCardActiveProSubtitle = "Підписка PRO активна • 14 днів відкрито",
            proCardLockedSubtitle = "Розширений прогноз погоди на 14 днів",
            proUnlockButtonLabel = "Оформити PRO",
            proLockBannerTitle = "Дні 8–14 доступні у Danny Weather PRO",
            proLockBannerSubtitle = "Або безкоштовно через VIP-список доступу",
            copyrightFooter = "© 2026 DenDev Studio. All rights reserved.",
            defaultVersionBanner = "Danny Weather • v2.0 (Оновлення Рідке Скло)",
            playStoreOpenError = "Не вдалося відкрити магазин додатків",
            googleConnectedStatus = "Google-акаунт підключено до хмарних оновлень",
            googleSignInSuccessToast = "Успішний вхід через Google",
            googleSignedOutStatus = "Вийшли з Google-акаунту",
            cloudSyncErrorFallback = "Помилка синхронізації хмари",
            weatherLoadErrorFallback = "Помилка оновлення погоди",
            accountUpdateTitle = "Потрібно оновити версію додатка",
            accountUpdateSubtitle = "Обліковий запис підключено • Перевірте нову версію",
            accountUpdateBody = "Ви під'єднали Danny Weather до хмарного облікового запису. Оновіть версію додатка через Google Play Market, щоб мати найновіші функції та синхронізацію.",
            playMarketLinkLabel = "Посилання на Google Play Market",
            openPlayMarketButton = "Оновити в Google Play Market",
            cloudAccountQuickLabel = "Хмарне сховище",
            cloudAccountConnectedTitle = "Хмарний акаунт підключено",
            cloudAccountDisconnectedTitle = "Швидкий вхід у хмарне сховище",
            cloudAccountSwitchButton = "Змінити акаунт",
            cloudAccountOneTapHint = "Вхід в 1 дотик для доступу до хмарного сховища та сповіщень про оновлення"
        )

        private fun english() = AppStrings(
            language = AppLanguage.ENGLISH,
            tabToday = "Today",
            tabSevenDays = "14 Days",
            tabWorldCities = "World",
            tabSettings = "Units",
            primaryCityBadge = "Primary city",
            pinActionLabel = "Pin",
            tapPinHint = "Tap 📌 to pin",
            pinTooltip = "Pin",
            unpinTooltip = "Unpin",
            loadingWeatherPrefix = "Updating weather:",
            loadingWeatherSubtitle = "Fetching data from weather stations",
            errorWeatherTitle = "Couldn't load weather",
            retryButton = "Try again",
            feelsLikePrefix = "Feels like",
            maxLabel = "High",
            minLabel = "Low",
            updatedPrefix = "Updated:",
            refreshWeatherCd = "Refresh weather",
            changeCityCd = "Change city",
            locationCd = "Location",
            hourlyTitle = "Hourly forecast",
            nowLabel = "Now",
            precipChanceCd = "Precipitation chance",
            sevenDayForecastPrefix = "14-day forecast",
            todayLabel = "Today",
            metricWindTitle = "WIND",
            windDirectionCd = "Wind direction",
            metricHumidityTitle = "HUMIDITY",
            humidityHigh = "High humidity",
            humidityDry = "Dry air",
            humidityComfort = "Comfortable",
            metricPressureTitle = "PRESSURE",
            pressureHigh = "High pressure",
            pressureLow = "Low pressure",
            pressureNormal = "Normal pressure",
            metricUvTitle = "UV INDEX",
            uvLow = "Low risk",
            uvModerate = "Moderate risk",
            uvHigh = "High risk",
            uvVeryHigh = "Very high risk",
            metricCloudsTitle = "CLOUDINESS",
            cloudsClear = "Clear sky",
            cloudsPartly = "Partly cloudy",
            cloudsOvercast = "Overcast",
            metricPrecipTitle = "PRECIPITATION",
            precipChancePrefix = "Chance:",
            sunriseTitle = "SUNRISE",
            sunsetTitle = "SUNSET",
            mmUnit = "mm",
            airQualityTitle = "Air Quality (AQI)",
            aqiSubtitle = "European air purity index",
            ozoneLabel = "Ozone O₃",
            microgramUnit = "µg/m³",
            firstLaunchTitle = "Choose a city or country",
            firstLaunchSubtitle = "Select the location whose weather you'd like to track",
            firstLaunchSearchPlaceholder = "Search city or country…",
            firstLaunchDetectedLangPrefix = "Device language:",
            firstLaunchSkip = "Skip",
            closeCd = "Close",
            clearCd = "Clear",
            citySheetTitle = "Cities & Pinned Locations",
            citySheetSubtitle = "Pick a city or tap 📌 to pin it on top",
            searchResultsHeader = "Worldwide search results",
            nothingFoundPrefix = "Nothing found for",
            worldFindCityTitle = "Explore cities & countries",
            worldFindCitySubtitle = "Search any city worldwide, read highlights, and pin favorites",
            worldSearchPlaceholder = "Type a city or country…",
            worldResultsHint = "Matching cities (tap to read about city)",
            worldRegionsHint = "Cities by region (tap to read about city)",
            worldEmptyHint = "No matches found. Try entering another city or country name.",
            aboutCityLabel = "About city",
            populationPrefix = "Population:",
            elevationPrefix = "Elevation:",
            metersUnit = "m",
            loadingCityInfoPrefix = "Loading details for",
            sourcePrefix = "Source:",
            showWeatherHere = "Weather here",
            pinnedLabel = "Pinned",
            pinToTopLabel = "Pin to top",
            settingsTitle = "Settings",
            settingsLanguageTitle = "App Language",
            settingsLanguageSystem = "Auto",
            settingsTempTitle = "Temperature",
            settingsTempCelsius = "°C Celsius",
            settingsTempFahrenheit = "°F Fahrenheit",
            settingsPressureTitle = "Atmospheric Pressure",
            settingsPressureMmhg = "mmHg",
            settingsPressureHpa = "hPa",
            settingsWindTitle = "Wind Speed",
            settingsWindMs = "m/s",
            settingsWindKmh = "km/h",
            cloudUpdatesTitle = "Cloud Updates",
            cloudConnectedPrefix = "Connected:",
            cloudSignInSubtitle = "Sign in to receive updates directly",
            signOutLabel = "Sign out",
            signInWithGoogleLabel = "Sign in with Google",
            aboutDescriptionText = "Weather for cities and countries worldwide. Liquid Glass interface, home screen widget, and ultra-fast performance.",
            updateAvailablePrefix = "New version available",
            updateDialogSubtitle = "Fresh Danny Weather improvements are ready to install",
            updateBannerSubtitle = "Update available",
            downloadPresetLabel = "Update",
            downloadShareLabel = "Update",
            shareApkLabel = "Update",
            openFileLabel = "Update",
            laterLabel = "Later",
            firstStepCompleted = "First step done",
            updateNowButton = "Update",
            honestForecastDisclaimer = "100% accuracy for the final 4–5 days of the forecast isn't possible because the Earth keeps spinning and weather is constantly changing",
            proCardTitle = "Danny Weather PRO",
            proCardActiveVipSubtitle = "Unlocked via VIP whitelist • 14-day forecast active",
            proCardActiveProSubtitle = "PRO subscription active • 14-day forecast unlocked",
            proCardLockedSubtitle = "Extended 14-day weather forecast access",
            proUnlockButtonLabel = "Get PRO",
            proLockBannerTitle = "Days 8–14 available in Danny Weather PRO",
            proLockBannerSubtitle = "Or free automatically for VIP-listed emails",
            copyrightFooter = "© 2026 DenDev Studio. All rights reserved.",
            defaultVersionBanner = "Danny Weather • v2.0 (Liquid Glass Update)",
            playStoreOpenError = "Could not open store page",
            googleConnectedStatus = "Google account connected to cloud updates",
            googleSignInSuccessToast = "Signed in with Google",
            googleSignedOutStatus = "Signed out of Google account",
            cloudSyncErrorFallback = "Cloud sync error",
            weatherLoadErrorFallback = "Failed to refresh weather",
            accountUpdateTitle = "App version update needed",
            accountUpdateSubtitle = "Account connected • Check for the latest version",
            accountUpdateBody = "Your app is connected to your cloud account. Please update Danny Weather to the latest version on Google Play Market.",
            playMarketLinkLabel = "Google Play Market link",
            openPlayMarketButton = "Update on Google Play Market",
            cloudAccountQuickLabel = "Cloud Storage",
            cloudAccountConnectedTitle = "Cloud account connected",
            cloudAccountDisconnectedTitle = "Quick Cloud Sign-In",
            cloudAccountSwitchButton = "Switch account",
            cloudAccountOneTapHint = "One-tap access to your cloud account and update notifications"
        )

        private fun polish() = english().copy(
            language = AppLanguage.POLISH,
            tabToday = "Dziś",
            tabSevenDays = "14 Dni",
            tabWorldCities = "Świat",
            tabSettings = "Jednostki",
            primaryCityBadge = "Główne miasto",
            pinActionLabel = "Przypnij",
            loadingWeatherPrefix = "Aktualizacja pogody:",
            loadingWeatherSubtitle = "Pobieranie danych ze stacji pogodowych",
            errorWeatherTitle = "Nie udało się pobrać pogody",
            retryButton = "Spróbuj ponownie",
            feelsLikePrefix = "Odczuwalna",
            maxLabel = "Maks",
            minLabel = "Min",
            updatedPrefix = "Zaktualizowano:",
            hourlyTitle = "Prognoza godzinowa",
            nowLabel = "Teraz",
            sevenDayForecastPrefix = "Prognoza 14-dniowa",
            todayLabel = "Dziś",
            metricWindTitle = "WIATR",
            metricHumidityTitle = "WILGOTNOŚĆ",
            metricPressureTitle = "CIŚNIENIE",
            metricUvTitle = "INDEKS UV",
            metricCloudsTitle = "ZACHMURZENIE",
            metricPrecipTitle = "OPADY",
            sunriseTitle = "WSCHÓD",
            sunsetTitle = "ZACHÓD",
            airQualityTitle = "Jakość powietrza (AQI)",
            firstLaunchTitle = "Wybierz miasto lub kraj",
            firstLaunchSubtitle = "Wybierz lokalizację, dla której chcesz sprawdzać pogodę",
            firstLaunchSearchPlaceholder = "Szukaj miasta lub kraju…",
            firstLaunchDetectedLangPrefix = "Język urządzenia:",
            firstLaunchSkip = "Pomiń",
            closeCd = "Zamknij",
            settingsTitle = "Ustawienia",
            settingsLanguageTitle = "Język aplikacji",
            settingsTempTitle = "Temperatura",
            settingsPressureTitle = "Ciśnienie",
            settingsWindTitle = "Prędkość wiatru",
            showWeatherHere = "Pogoda tutaj",
            defaultVersionBanner = "Danny Weather • v2.0 (Aktualizacja Liquid Glass)",
            playStoreOpenError = "Nie udało się otworzyć strony aktualizacji",
            googleConnectedStatus = "Konto Google połączone z aktualizacjami w chmurze",
            googleSignInSuccessToast = "Zalogowano przez Google",
            googleSignedOutStatus = "Wylogowano z konta Google",
            cloudSyncErrorFallback = "Błąd synchronizacji z chmurą",
            weatherLoadErrorFallback = "Nie udało się odświeżyć pogody",
            accountUpdateTitle = "Wymagana aktualizacja aplikacji",
            accountUpdateSubtitle = "Konto połączone • Sprawdź najnowszą wersję",
            accountUpdateBody = "Twoja aplikacja jest połączona z kontem w chmurze. Zaktualizuj Danny Weather do najnowszej wersji w Google Play Market.",
            playMarketLinkLabel = "Link do Google Play Market",
            openPlayMarketButton = "Otwórz Google Play Market",
            cloudAccountQuickLabel = "Pamięć w chmurze",
            cloudAccountConnectedTitle = "Konto w chmurze połączone",
            cloudAccountDisconnectedTitle = "Szybkie logowanie do chmury",
            cloudAccountSwitchButton = "Zmień konto",
            cloudAccountOneTapHint = "Dostęp jednym dotknięciem do chmury i powiadomień o aktualizacjach"
        )

        private fun german() = english().copy(
            language = AppLanguage.GERMAN,
            tabToday = "Heute",
            tabSevenDays = "14 Tage",
            tabWorldCities = "Welt",
            tabSettings = "Einheiten",
            primaryCityBadge = "Hauptstadt",
            pinActionLabel = "Anheften",
            loadingWeatherPrefix = "Wetter wird aktualisiert:",
            errorWeatherTitle = "Wetter konnte nicht geladen werden",
            retryButton = "Erneut versuchen",
            feelsLikePrefix = "Gefühlt wie",
            updatedPrefix = "Aktualisiert:",
            hourlyTitle = "Stündliche Vorhersage",
            nowLabel = "Jetzt",
            sevenDayForecastPrefix = "14-Tage-Vorhersage",
            todayLabel = "Heute",
            metricWindTitle = "WIND",
            metricHumidityTitle = "LUFTFEUCHTIGKEIT",
            metricPressureTitle = "LUFTDRUCK",
            metricUvTitle = "UV-INDEX",
            metricCloudsTitle = "BEWÖLKUNG",
            metricPrecipTitle = "NIEDERSCHLAG",
            sunriseTitle = "SONNENAUFGANG",
            sunsetTitle = "SONNENUNTERGANG",
            airQualityTitle = "Luftqualität (AQI)",
            firstLaunchTitle = "Stadt oder Land wählen",
            firstLaunchSubtitle = "Wählen Sie einen Ort für die Wettervorhersage",
            firstLaunchSearchPlaceholder = "Stadt oder Land suchen…",
            firstLaunchDetectedLangPrefix = "Gerätesprache:",
            firstLaunchSkip = "Überspringen",
            closeCd = "Schließen",
            settingsTitle = "Einstellungen",
            settingsLanguageTitle = "App-Sprache",
            showWeatherHere = "Wetter hier",
            defaultVersionBanner = "Danny Weather • v2.0 (Liquid Glass Update)",
            playStoreOpenError = "App-Seite konnte nicht geöffnet werden",
            googleConnectedStatus = "Google-Konto mit Cloud-Updates verbunden",
            googleSignInSuccessToast = "Mit Google angemeldet",
            googleSignedOutStatus = "Vom Google-Konto abgemeldet",
            cloudSyncErrorFallback = "Cloud-Synchronisierungsfehler",
            weatherLoadErrorFallback = "Wetter konnte nicht aktualisiert werden",
            accountUpdateTitle = "App-Update erforderlich",
            accountUpdateSubtitle = "Konto verbunden • Neueste Version prüfen",
            accountUpdateBody = "Deine App ist mit deinem Cloud-Konto verbunden. Bitte aktualisiere Danny Weather auf die neueste Version im Google Play Market.",
            playMarketLinkLabel = "Google Play Market Link",
            openPlayMarketButton = "Im Google Play Market aktualisieren",
            cloudAccountQuickLabel = "Cloud-Speicher",
            cloudAccountConnectedTitle = "Cloud-Konto verbunden",
            cloudAccountDisconnectedTitle = "Schnelle Cloud-Anmeldung",
            cloudAccountSwitchButton = "Konto wechseln",
            cloudAccountOneTapHint = "Ein-Tipp-Zugriff auf dein Cloud-Konto und Update-Hinweise"
        )

        private fun spanish() = english().copy(
            language = AppLanguage.SPANISH,
            tabToday = "Hoy",
            tabSevenDays = "14 Días",
            tabWorldCities = "Mundo",
            tabSettings = "Unidades",
            primaryCityBadge = "Ciudad principal",
            pinActionLabel = "Fijar",
            loadingWeatherPrefix = "Actualizando clima:",
            errorWeatherTitle = "No se pudo cargar el clima",
            retryButton = "Reintentar",
            feelsLikePrefix = "Sensación térmica",
            updatedPrefix = "Actualizado:",
            hourlyTitle = "Pronóstico por hora",
            nowLabel = "Ahora",
            sevenDayForecastPrefix = "Pronóstico de 14 días",
            todayLabel = "Hoy",
            metricWindTitle = "VIENTO",
            metricHumidityTitle = "HUMEDAD",
            metricPressureTitle = "PRESIÓN",
            metricUvTitle = "ÍNDICE UV",
            metricCloudsTitle = "NUBOSIDAD",
            metricPrecipTitle = "PRECIPITACIÓN",
            sunriseTitle = "AMANECER",
            sunsetTitle = "ATARDECER",
            airQualityTitle = "Calidad del aire (AQI)",
            firstLaunchTitle = "Elige una ciudad o país",
            firstLaunchSubtitle = "Selecciona la ubicación cuyo clima deseas ver",
            firstLaunchSearchPlaceholder = "Buscar ciudad o país…",
            firstLaunchDetectedLangPrefix = "Idioma del teléfono:",
            firstLaunchSkip = "Omitir",
            closeCd = "Cerrar",
            settingsTitle = "Ajustes",
            settingsLanguageTitle = "Idioma de la app",
            showWeatherHere = "Ver clima",
            defaultVersionBanner = "Danny Weather • v2.0 (Actualización Liquid Glass)",
            playStoreOpenError = "No se pudo abrir la página de actualización",
            googleConnectedStatus = "Cuenta de Google conectada a actualizaciones en la nube",
            googleSignInSuccessToast = "Sesión iniciada con Google",
            googleSignedOutStatus = "Sesión de Google cerrada",
            cloudSyncErrorFallback = "Error de sincronización en la nube",
            weatherLoadErrorFallback = "No se pudo actualizar el clima",
            accountUpdateTitle = "Actualización de la app necesaria",
            accountUpdateSubtitle = "Cuenta conectada • Verifica la última versión",
            accountUpdateBody = "Tu app está conectada a tu cuenta en la nube. Actualiza Danny Weather a la última versión en Google Play Market.",
            playMarketLinkLabel = "Enlace de Google Play Market",
            openPlayMarketButton = "Actualizar en Google Play Market",
            cloudAccountQuickLabel = "Almacenamiento en la nube",
            cloudAccountConnectedTitle = "Cuenta en la nube conectada",
            cloudAccountDisconnectedTitle = "Inicio rápido en la nube",
            cloudAccountSwitchButton = "Cambiar cuenta",
            cloudAccountOneTapHint = "Acceso con un toque a tu cuenta en la nube y avisos de actualización"
        )

        private fun french() = english().copy(
            language = AppLanguage.FRENCH,
            tabToday = "Aujourd'hui",
            tabSevenDays = "14 Jours",
            tabWorldCities = "Monde",
            tabSettings = "Unités",
            primaryCityBadge = "Ville principale",
            pinActionLabel = "Épingler",
            loadingWeatherPrefix = "Mise à jour météo :",
            errorWeatherTitle = "Impossible de charger la météo",
            retryButton = "Réessayer",
            feelsLikePrefix = "Ressenti",
            updatedPrefix = "Mis à jour :",
            hourlyTitle = "Prévisions horaires",
            nowLabel = "Maintenant",
            sevenDayForecastPrefix = "Prévisions sur 14 jours",
            todayLabel = "Aujourd'hui",
            metricWindTitle = "VENT",
            metricHumidityTitle = "HUMIDITÉ",
            metricPressureTitle = "PRESSION",
            metricUvTitle = "INDICE UV",
            metricCloudsTitle = "NÉBULOSITÉ",
            metricPrecipTitle = "PRÉCIPITATIONS",
            sunriseTitle = "LEVER",
            sunsetTitle = "COUCHER",
            airQualityTitle = "Qualité de l'air (AQI)",
            firstLaunchTitle = "Choisissez une ville ou un pays",
            firstLaunchSubtitle = "Sélectionnez le lieu dont vous souhaitez voir la météo",
            firstLaunchSearchPlaceholder = "Rechercher ville ou pays…",
            firstLaunchDetectedLangPrefix = "Langue détectée :",
            firstLaunchSkip = "Ignorer",
            closeCd = "Fermer",
            settingsTitle = "Paramètres",
            settingsLanguageTitle = "Langue de l'app",
            showWeatherHere = "Météo ici",
            defaultVersionBanner = "Danny Weather • v2.0 (Mise à jour Liquid Glass)",
            playStoreOpenError = "Impossible d'ouvrir la page de mise à jour",
            googleConnectedStatus = "Compte Google connecté aux mises à jour cloud",
            googleSignInSuccessToast = "Connecté avec Google",
            googleSignedOutStatus = "Déconnecté du compte Google",
            cloudSyncErrorFallback = "Erreur de synchronisation cloud",
            weatherLoadErrorFallback = "Impossible d'actualiser la météo",
            accountUpdateTitle = "Mise à jour de l'app requise",
            accountUpdateSubtitle = "Compte connecté • Vérifiez la dernière version",
            accountUpdateBody = "Votre application est connectée au compte cloud. Mettez à jour Danny Weather vers la dernière version sur Google Play Market.",
            playMarketLinkLabel = "Lien Google Play Market",
            openPlayMarketButton = "Mettre à jour sur Google Play Market",
            cloudAccountQuickLabel = "Stockage cloud",
            cloudAccountConnectedTitle = "Compte cloud connecté",
            cloudAccountDisconnectedTitle = "Connexion cloud rapide",
            cloudAccountSwitchButton = "Changer de compte",
            cloudAccountOneTapHint = "Accès en un geste à votre compte cloud et aux alertes de mise à jour"
        )
    }
}

val LocalAppStrings = staticCompositionLocalOf {
    AppStrings.forLanguage(AppLanguage.fromLocale())
}

fun filterCitiesLocally(
    cities: List<City>,
    query: String,
    language: AppLanguage
): List<City> {
    val q = query.trim().lowercase(Locale.ROOT)
    if (q.isBlank()) return cities
    return cities.filter { city ->
        val countryEn = if (city.countryCode.length == 2) {
            Locale.Builder().setRegion(city.countryCode).build()
                .getDisplayCountry(Locale.ENGLISH).lowercase(Locale.ROOT)
        } else ""
        val countryActive = city.localizedCountry(language).lowercase(Locale.ROOT)
        city.nameUk.lowercase(Locale.ROOT).contains(q) ||
            city.nameEn.lowercase(Locale.ROOT).contains(q) ||
            city.countryUk.lowercase(Locale.ROOT).contains(q) ||
            city.regionUk.lowercase(Locale.ROOT).contains(q) ||
            countryEn.contains(q) ||
            countryActive.contains(q) ||
            city.countryCode.lowercase(Locale.ROOT) == q
    }
}

fun formatLocalizedDayOfWeek(
    isoDate: String,
    fallbackDayUk: String,
    isToday: Boolean,
    strings: AppStrings
): String {
    if (isToday || fallbackDayUk == "Сьогодні") return strings.todayLabel
    if (strings.language == AppLanguage.UKRAINIAN || isoDate.isBlank()) return fallbackDayUk
    return try {
        val ld = java.time.LocalDate.parse(isoDate)
        ld.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, strings.language.locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(strings.language.locale) else it.toString() }
    } catch (_: Exception) {
        fallbackDayUk
    }
}

fun formatLocalizedDateLabel(
    isoDate: String,
    fallbackDateLabel: String,
    language: AppLanguage
): String {
    if (language == AppLanguage.UKRAINIAN || isoDate.isBlank()) return fallbackDateLabel
    return try {
        val ld = java.time.LocalDate.parse(isoDate)
        ld.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", language.locale))
    } catch (_: Exception) {
        fallbackDateLabel
    }
}
