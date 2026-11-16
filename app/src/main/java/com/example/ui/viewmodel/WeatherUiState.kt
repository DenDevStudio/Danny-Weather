package com.example.ui.viewmodel

import com.example.data.model.AppCustomizationConfig
import com.example.data.model.City
import com.example.data.model.CityArticleInfo
import com.example.data.model.DEFAULT_CITY
import com.example.data.model.FullWeatherData
import com.example.data.model.PRESET_CITIES
import com.example.data.model.ReleaseUpdateNotice
import com.example.data.model.SkyType
import com.example.data.model.UserWeatherSettings
import com.example.data.model.WeatherHomeBlock
import com.example.data.model.WorldRegion
import com.example.data.repository.CloudVipAccessEntry
import com.example.ui.i18n.AppLanguage

data class WeatherUiState(
    val selectedCity: City = DEFAULT_CITY,
    val weatherData: FullWeatherData? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val settings: UserWeatherSettings = UserWeatherSettings(),
    val isCityPickerVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val selectedWorldRegion: WorldRegion = WorldRegion.ALL,
    val presetCities: List<City> = PRESET_CITIES,
    val pinnedCities: List<City> = emptyList(),
    val inspectedCity: City = DEFAULT_CITY,
    val cityArticle: CityArticleInfo? = null,
    val isLoadingArticle: Boolean = false,
    val isSecretLoginVisible: Boolean = false,
    val isSecretMenuUnlocked: Boolean = false,
    val isAdminMode: Boolean = false,
    val isAdminLegacyFrameless: Boolean = false,
    val secretLoginError: String? = null,
    val secretStatusMessage: String? = null,
    val debugSkyOverride: SkyType? = null,
    val homeBlockOrder: List<WeatherHomeBlock> = WeatherHomeBlock.DEFAULT_ORDER,
    val releaseUpdateNotice: ReleaseUpdateNotice = ReleaseUpdateNotice(),
    val hasUnpublishedAdminChanges: Boolean = false,
    val customization: AppCustomizationConfig = AppCustomizationConfig(),
    val isGoogleSignedIn: Boolean = false,
    val cloudUserEmail: String? = null,
    val isCloudSyncing: Boolean = false,
    val cloudSyncError: String? = null,
    val vipEntries: List<CloudVipAccessEntry> = emptyList(),
    val isVipWhitelisted: Boolean = false,
    val isProSubscribed: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.fromLocale(),
    val appLanguageOverride: AppLanguage? = null,
    val isFirstLaunchPickerVisible: Boolean = false,
    val isAccountUpdateBannerDismissed: Boolean = false,
    val pendingConnectedAccountNotice: Boolean = false
) {
    val isProAccessGranted: Boolean
        get() = isAdminMode || isVipWhitelisted || isProSubscribed
}
