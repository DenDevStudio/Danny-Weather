package com.example.data.model

enum class WorldRegion(val titleUk: String, val iconEmoji: String) {
    ALL("Усі країни", "🌍"),
    AMERICAS("Америка", "🗽"),
    EUROPE("Європа", "🏰"),
    ASIA("Азія", "🏯"),
    AFRICA("Африка", "🏜️"),
    OCEANIA("Океанія", "🏝️"),
    UKRAINE("Україна", "🇺🇦")
}

fun countryCodeToFlag(countryCode: String?): String {
    if (countryCode == null || countryCode.length != 2) return "🌍"
    val upper = countryCode.uppercase()
    if (upper[0] !in 'A'..'Z' || upper[1] !in 'A'..'Z') return "🌍"
    val first = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

data class City(
    val id: String,
    val nameUk: String,
    val nameEn: String,
    val regionUk: String,
    val latitude: Double,
    val longitude: Double,
    val countryUk: String = "",
    val countryCode: String = "",
    val worldRegion: WorldRegion = WorldRegion.AMERICAS,
    val isPrimary: Boolean = false,
    val description: String = "",
    val population: Long? = null,
    val elevation: Double? = null,
    val timezone: String? = null
) {
    val flagEmoji: String
        get() = countryCodeToFlag(countryCode)
}

data class CityArticleInfo(
    val city: City,
    val title: String,
    val summary: String,
    val sourceLabel: String,
    val articleUrl: String? = null
)

val NEW_YORK_CITY = City(
    id = "new_york_us",
    nameUk = "Нью-Йорк",
    nameEn = "New York",
    regionUk = "Нью-Йорк, США",
    latitude = 40.7128,
    longitude = -74.0060,
    countryUk = "США",
    countryCode = "US",
    worldRegion = WorldRegion.AMERICAS,
    description = "Найбільше місто США та діловий центр штату Нью-Йорк."
)

val KYIV_CITY = City(
    id = "kyiv_ua",
    nameUk = "Київ",
    nameEn = "Kyiv",
    regionUk = "Київ, Україна",
    latitude = 50.4501,
    longitude = 30.5234,
    countryUk = "Україна",
    countryCode = "UA",
    worldRegion = WorldRegion.UKRAINE,
    description = "Столиця України на річці Дніпро."
)

val STRYI_CITY = City(
    id = "stryi_lviv",
    nameUk = "Стрий",
    nameEn = "Stryi",
    regionUk = "Львівська область, Україна",
    latitude = 49.2558,
    longitude = 23.8533,
    countryUk = "Україна",
    countryCode = "UA",
    worldRegion = WorldRegion.UKRAINE,
    isPrimary = false,
    description = "Місто на річці Стрий у Львівській області."
)

val DEFAULT_CITY: City = NEW_YORK_CITY

val PRESET_CITIES: List<City> = WORLD_CITIES

enum class TemperatureUnit(val symbol: String, val labelUk: String) {
    CELSIUS("°C", "Цельсій (°C)"),
    FAHRENHEIT("°F", "Фаренгейт (°F)")
}

enum class PressureUnit(val symbol: String, val labelUk: String) {
    MMHG("мм рт. ст.", "Міліметри рт. ст."),
    HPA("гПа", "Гектопаскалі")
}

enum class WindUnit(val symbol: String, val labelUk: String) {
    MS("м/с", "Метри за секунду"),
    KMH("км/год", "Кілометри на годину")
}

data class UserWeatherSettings(
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val pressureUnit: PressureUnit = PressureUnit.MMHG,
    val windUnit: WindUnit = WindUnit.MS
)

enum class SkyType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    OVERCAST,
    FOG,
    RAIN,
    HEAVY_RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherCondition(
    val code: Int,
    val nameUk: String,
    val skyType: SkyType,
    val iconKey: String
)

data class CurrentWeather(
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidityPercent: Int,
    val pressureHpa: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val cloudCoverPercent: Int,
    val precipitationMm: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val uvIndex: Double,
    val lastUpdatedTime: String
)

data class HourlyForecastItem(
    val timeLabel: String,
    val fullTime: String,
    val tempCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProb: Int,
    val windSpeedKmh: Double,
    val isDay: Boolean,
    val isNow: Boolean = false
)

data class DailyForecastItem(
    val dateLabel: String,
    val dayOfWeekUk: String,
    val maxTempCelsius: Double,
    val minTempCelsius: Double,
    val condition: WeatherCondition,
    val precipitationProbMax: Int,
    val precipitationSumMm: Double,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val windSpeedMaxKmh: Double,
    val isoDate: String = "",
    val isToday: Boolean = false
)

data class AirQualityInfo(
    val europeanAqi: Int,
    val levelUk: String,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val nitrogenDioxide: Double,
    val adviceUk: String
)

data class FullWeatherData(
    val city: City,
    val current: CurrentWeather,
    val hourly: List<HourlyForecastItem>,
    val daily: List<DailyForecastItem>,
    val airQuality: AirQualityInfo?
)

enum class WeatherHomeBlock(val titleUk: String) {
    CURRENT("Поточна погода"),
    HOURLY("Погодинний прогноз"),
    METRICS("Детальні показники"),
    AIR_QUALITY("Якість повітря (AQI)");

    companion object {
        val DEFAULT_ORDER: List<WeatherHomeBlock> = listOf(CURRENT, HOURLY, METRICS, AIR_QUALITY)
    }
}

data class ReleaseUpdateNotice(
    val isAvailable: Boolean = false,
    val targetVersion: String = "2.1",
    val releaseNotes: String = ""
)

enum class AppAccentTheme(val labelUk: String, val colorHex: Long) {
    GOLD("Золотий", 0xFFFFD54FL),
    SKY("Небесний", 0xFF38BDF8L),
    EMERALD("Смарагд", 0xFF4ADE80L),
    LAVENDER("Лаванда", 0xFFC084FCL),
    CORAL("Корал", 0xFFFB7185L),
    CRYSTAL("Кришталь", 0xFFF8FAFCL);

    companion object {
        fun fromName(name: String?): AppAccentTheme =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GOLD
    }
}

data class AppCustomizationConfig(
    val appTitle: String = "Danny Weather",
    val headerBadge: String = "v2.0",
    val versionBannerText: String = DEFAULT_VERSION_BANNER_UK,
    val hiddenHomeBlocks: Set<WeatherHomeBlock> = emptySet(),
    val showPinnedStrip: Boolean = true,
    val showWorldCitiesTab: Boolean = true,
    val accentTheme: AppAccentTheme = AppAccentTheme.GOLD,
    val cardCornerRadiusDp: Int = 26,
    val glassOpacityLevel: Float = 1.0f,
    val animateLiquidBackground: Boolean = true,
    val savedSkyOverride: SkyType? = null
) {
    companion object {
        const val DEFAULT_VERSION_BANNER_UK = "Danny Weather • v2.0 (Оновлення Рідке Скло)"
        const val LEGACY_V21_VERSION_BANNER_UK = "Danny Weather • v2.1 (Хмарне оновлення)"
    }
}
