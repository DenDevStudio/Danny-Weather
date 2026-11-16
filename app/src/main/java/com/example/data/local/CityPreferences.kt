package com.example.data.local

import android.content.Context
import com.example.data.model.AppCustomizationConfig
import com.example.data.model.City
import com.example.data.model.DEFAULT_CITY
import com.example.data.model.ReleaseUpdateNotice
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherHomeBlock
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class SavedCityDto(
    val id: String,
    val nameUk: String,
    val nameEn: String,
    val regionUk: String,
    val latitude: Double,
    val longitude: Double,
    val countryUk: String = "",
    val countryCode: String = "",
    val description: String = "",
    val population: Long? = null,
    val elevation: Double? = null,
    val timezone: String? = null
)

object CityPreferences {
    private const val PREFS_NAME = "danny_weather_prefs"
    private const val KEY_CITY_ID = "selected_city_id"
    private const val KEY_CITY_NAME_UK = "selected_city_name_uk"
    private const val KEY_CITY_NAME_EN = "selected_city_name_en"
    private const val KEY_CITY_REGION = "selected_city_region"
    private const val KEY_CITY_COUNTRY = "selected_city_country"
    private const val KEY_CITY_COUNTRY_CODE = "selected_city_country_code"
    private const val KEY_CITY_LAT = "selected_city_lat"
    private const val KEY_CITY_LON = "selected_city_lon"
    private const val KEY_PINNED_CITIES_JSON = "pinned_cities_json"
    private const val KEY_PINNED_INITIALIZED = "pinned_cities_initialized"
    private const val KEY_HOME_BLOCK_ORDER = "home_block_order"
    private const val KEY_TEMP_UNIT = "temp_unit"
    private const val KEY_UPDATE_AVAILABLE = "play_update_available"
    private const val KEY_UPDATE_VERSION = "play_update_version"
    private const val KEY_UPDATE_NOTES = "play_update_notes"

    private val moshi: Moshi by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }

    private val pinnedAdapter by lazy {
        val listType = Types.newParameterizedType(List::class.java, SavedCityDto::class.java)
        moshi.adapter<List<SavedCityDto>>(listType)
    }

    fun saveSelectedCity(context: Context, city: City) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CITY_ID, city.id)
            .putString(KEY_CITY_NAME_UK, city.nameUk)
            .putString(KEY_CITY_NAME_EN, city.nameEn)
            .putString(KEY_CITY_REGION, city.regionUk)
            .putString(KEY_CITY_COUNTRY, city.countryUk)
            .putString(KEY_CITY_COUNTRY_CODE, city.countryCode)
            .putString(KEY_CITY_LAT, city.latitude.toString())
            .putString(KEY_CITY_LON, city.longitude.toString())
            .apply()
    }

    fun getSelectedCity(context: Context): City {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_CITY_ID, null) ?: return DEFAULT_CITY
        val nameUk = prefs.getString(KEY_CITY_NAME_UK, null) ?: return DEFAULT_CITY
        val nameEn = prefs.getString(KEY_CITY_NAME_EN, nameUk) ?: nameUk
        val regionUk = prefs.getString(KEY_CITY_REGION, "") ?: ""
        val countryUk = prefs.getString(KEY_CITY_COUNTRY, "") ?: ""
        val countryCode = prefs.getString(KEY_CITY_COUNTRY_CODE, "") ?: ""
        val lat = prefs.getString(KEY_CITY_LAT, null)?.toDoubleOrNull() ?: return DEFAULT_CITY
        val lon = prefs.getString(KEY_CITY_LON, null)?.toDoubleOrNull() ?: return DEFAULT_CITY

        return City(
            id = id,
            nameUk = nameUk,
            nameEn = nameEn,
            regionUk = regionUk,
            latitude = lat,
            longitude = lon,
            countryUk = countryUk,
            countryCode = countryCode,
            isPrimary = false
        )
    }

    fun getPinnedCities(context: Context): List<City> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PINNED_INITIALIZED, false)) {
            savePinnedCities(context, emptyList())
            return emptyList()
        }
        val json = prefs.getString(KEY_PINNED_CITIES_JSON, null) ?: return emptyList()
        return try {
            pinnedAdapter.fromJson(json)
                ?.filterNot { it.id == "berezhany_ternopil" }
                ?.map { dto ->
                    City(
                        id = dto.id,
                        nameUk = dto.nameUk,
                        nameEn = dto.nameEn,
                        regionUk = dto.regionUk,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        countryUk = dto.countryUk,
                        countryCode = dto.countryCode,
                        description = dto.description,
                        population = dto.population,
                        elevation = dto.elevation,
                        timezone = dto.timezone,
                        isPrimary = false
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePinnedCities(context: Context, cities: List<City>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dtos = cities.distinctBy { it.id }.map { city ->
            SavedCityDto(
                id = city.id,
                nameUk = city.nameUk,
                nameEn = city.nameEn,
                regionUk = city.regionUk,
                latitude = city.latitude,
                longitude = city.longitude,
                countryUk = city.countryUk,
                countryCode = city.countryCode,
                description = city.description,
                population = city.population,
                elevation = city.elevation,
                timezone = city.timezone
            )
        }
        val json = pinnedAdapter.toJson(dtos)
        prefs.edit()
            .putBoolean(KEY_PINNED_INITIALIZED, true)
            .putString(KEY_PINNED_CITIES_JSON, json)
            .apply()
    }

    fun saveTempUnit(context: Context, unit: TemperatureUnit) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TEMP_UNIT, unit.name)
            .apply()
    }

    fun getTempUnit(context: Context): TemperatureUnit {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TEMP_UNIT, null)
        return TemperatureUnit.entries.firstOrNull { it.name == raw } ?: TemperatureUnit.CELSIUS
    }

    fun saveHomeBlockOrder(context: Context, order: List<WeatherHomeBlock>) {
        val serialized = order.joinToString(",") { it.name }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOME_BLOCK_ORDER, serialized)
            .apply()
    }

    fun getHomeBlockOrder(context: Context): List<WeatherHomeBlock> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HOME_BLOCK_ORDER, null) ?: return WeatherHomeBlock.DEFAULT_ORDER
        val parsed = raw.split(",")
            .mapNotNull { name -> WeatherHomeBlock.entries.firstOrNull { it.name == name } }
            .distinct()
        return if (parsed.size == WeatherHomeBlock.entries.size) {
            parsed
        } else {
            WeatherHomeBlock.DEFAULT_ORDER
        }
    }

    fun saveReleaseUpdateNotice(context: Context, notice: ReleaseUpdateNotice) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_UPDATE_AVAILABLE, notice.isAvailable)
            .putString(KEY_UPDATE_VERSION, notice.targetVersion)
            .putString(KEY_UPDATE_NOTES, notice.releaseNotes)
            .apply()
    }

    fun getReleaseUpdateNotice(context: Context): ReleaseUpdateNotice {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ReleaseUpdateNotice(
            isAvailable = prefs.getBoolean(KEY_UPDATE_AVAILABLE, false),
            targetVersion = prefs.getString(KEY_UPDATE_VERSION, "2.1") ?: "2.1",
            releaseNotes = prefs.getString(KEY_UPDATE_NOTES, "") ?: ""
        )
    }

    fun saveCustomizationConfig(context: Context, config: com.example.data.model.AppCustomizationConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("custom_app_title", config.appTitle)
            .putString("custom_header_badge", config.headerBadge)
            .putString("custom_version_banner", config.versionBannerText)
            .putString("custom_hidden_blocks", config.hiddenHomeBlocks.joinToString(",") { it.name })
            .putBoolean("custom_show_pinned_strip", config.showPinnedStrip)
            .putBoolean("custom_show_world_tab", config.showWorldCitiesTab)
            .putString("custom_accent_theme", config.accentTheme.name)
            .putInt("custom_corner_radius", config.cardCornerRadiusDp)
            .putFloat("custom_glass_opacity", config.glassOpacityLevel)
            .putBoolean("custom_animate_liquid", config.animateLiquidBackground)
            .putString("custom_saved_sky", config.savedSkyOverride?.name ?: "")
            .apply()
    }

    fun getCustomizationConfig(context: Context): com.example.data.model.AppCustomizationConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hiddenRaw = prefs.getString("custom_hidden_blocks", "") ?: ""
        val hiddenSet = hiddenRaw.split(",")
            .mapNotNull { token -> WeatherHomeBlock.entries.firstOrNull { it.name == token } }
            .toSet()
        val skyRaw = prefs.getString("custom_saved_sky", "") ?: ""
        val savedSky = com.example.data.model.SkyType.entries.firstOrNull { it.name == skyRaw }
        val rawBadge = prefs.getString("custom_header_badge", "v2.0") ?: "v2.0"
        val resolvedBadge = if (rawBadge in setOf("V2", "V2.1", "v2.1", "V2.0")) "v2.0" else rawBadge
        val rawBanner = prefs.getString(
            "custom_version_banner",
            AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK
        ) ?: AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK
        val resolvedBanner = if (rawBanner == AppCustomizationConfig.LEGACY_V21_VERSION_BANNER_UK) {
            AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK
        } else {
            rawBanner
        }
        return com.example.data.model.AppCustomizationConfig(
            appTitle = prefs.getString("custom_app_title", "Danny Weather") ?: "Danny Weather",
            headerBadge = resolvedBadge,
            versionBannerText = resolvedBanner,
            hiddenHomeBlocks = hiddenSet,
            showPinnedStrip = prefs.getBoolean("custom_show_pinned_strip", true),
            showWorldCitiesTab = prefs.getBoolean("custom_show_world_tab", true),
            accentTheme = com.example.data.model.AppAccentTheme.fromName(
                prefs.getString("custom_accent_theme", "GOLD")
            ),
            cardCornerRadiusDp = prefs.getInt("custom_corner_radius", 26).coerceIn(12, 36),
            glassOpacityLevel = prefs.getFloat("custom_glass_opacity", 1.0f).coerceIn(0.4f, 1.6f),
            animateLiquidBackground = prefs.getBoolean("custom_animate_liquid", true),
            savedSkyOverride = savedSky
        )
    }

    private const val KEY_FIRST_LAUNCH_COMPLETED = "first_launch_city_setup_completed"
    private const val KEY_APP_LANGUAGE_MODE = "app_language_mode"
    private const val KEY_ADMIN_SESSION_ACTIVE = "admin_session_active"
    private const val KEY_ADMIN_LEGACY_FRAMELESS = "admin_legacy_frameless"
    private const val KEY_PRO_SUBSCRIBED = "danny_weather_pro_subscribed"
    private const val KEY_VIP_WHITELISTED_CACHE = "danny_weather_vip_whitelisted_cache"
    private const val KEY_CONNECTED_UPDATE_NOTIF_KEY = "connected_account_update_notif_key"

    fun hasNotifiedConnectedAccount(context: Context, key: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CONNECTED_UPDATE_NOTIF_KEY, null) == key
    }

    fun markConnectedAccountNotified(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONNECTED_UPDATE_NOTIF_KEY, key)
            .apply()
    }

    fun saveAdminSession(
        context: Context,
        isAdmin: Boolean,
        isLegacyFrameless: Boolean
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ADMIN_SESSION_ACTIVE, isAdmin)
            .putBoolean(KEY_ADMIN_LEGACY_FRAMELESS, isLegacyFrameless)
            .apply()
    }

    fun isAdminSessionActive(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ADMIN_SESSION_ACTIVE, false)
    }

    fun isAdminLegacyFramelessActive(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ADMIN_LEGACY_FRAMELESS, true)
    }

    fun saveProSubscribed(context: Context, subscribed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PRO_SUBSCRIBED, subscribed)
            .apply()
    }

    fun isProSubscribed(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PRO_SUBSCRIBED, false)
    }

    fun saveVipWhitelistedCache(context: Context, isVip: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_VIP_WHITELISTED_CACHE, isVip)
            .apply()
    }

    fun isVipWhitelistedCache(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VIP_WHITELISTED_CACHE, false)
    }

    fun isFirstLaunchCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)
    }

    fun setFirstLaunchCompleted(context: Context, completed: Boolean = true) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FIRST_LAUNCH_COMPLETED, completed)
            .apply()
    }

    fun saveAppLanguageMode(context: Context, language: com.example.ui.i18n.AppLanguage?) {
        val raw = language?.name ?: com.example.ui.i18n.AppLanguage.SYSTEM_MODE
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE_MODE, raw)
            .apply()
    }

    fun getAppLanguageOverride(context: Context): com.example.ui.i18n.AppLanguage? {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE_MODE, com.example.ui.i18n.AppLanguage.SYSTEM_MODE)
        return com.example.ui.i18n.AppLanguage.fromStorage(raw)
    }

    fun resolveInitialAppLanguage(context: Context): com.example.ui.i18n.AppLanguage {
        return getAppLanguageOverride(context)
            ?: com.example.ui.i18n.AppLanguage.fromConfiguration(context)
    }
}
