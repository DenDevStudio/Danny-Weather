package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.OpenMeteoApi
import com.example.data.local.CityPreferences
import com.example.data.local.WeatherDatabase
import com.example.data.model.AppAccentTheme
import com.example.data.model.AppCustomizationConfig
import com.example.data.model.City
import com.example.data.model.PressureUnit
import com.example.data.model.ReleaseUpdateNotice
import com.example.data.model.STRYI_CITY
import com.example.data.model.SkyType
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherHomeBlock
import com.example.data.model.WindUnit
import com.example.data.model.WorldRegion
import com.example.data.repository.CloudReleaseRepository
import com.example.data.repository.WeatherRepository
import com.example.ui.components.SecretCredentials
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.filterCitiesLocally
import com.example.util.SecurityWebhookReporter
import com.example.widget.DannyWeatherWidgetProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeatherRepository by lazy {
        val database = WeatherDatabase.getInstance(application)
        val api = OpenMeteoApi.create()
        WeatherRepository(api, database.weatherDao())
    }

    private val cloudReleaseRepository: CloudReleaseRepository by lazy {
        CloudReleaseRepository(application)
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var articleJob: Job? = null
    private var cloudReleaseJob: Job? = null
    private var ownVipJob: Job? = null
    private var adminVipListJob: Job? = null
    private var nonAdminCity: City? = null
    private var nonAdminPinnedCities: List<City>? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            _uiState.update {
                it.copy(
                    isGoogleSignedIn = true,
                    cloudUserEmail = user.email,
                    cloudSyncError = null
                )
            }
            startOwnVipObserver()
            if (_uiState.value.isAdminMode) {
                startAdminVipListObserver()
            }
        } else {
            stopOwnVipObserver()
            stopAdminVipListObserver()
            CityPreferences.saveVipWhitelistedCache(getApplication(), false)
            _uiState.update {
                it.copy(
                    isGoogleSignedIn = false,
                    cloudUserEmail = null,
                    isVipWhitelisted = false,
                    vipEntries = emptyList(),
                    pendingConnectedAccountNotice = false
                )
            }
        }
    }

    init {
        val savedCity = CityPreferences.getSelectedCity(application)
        val savedPinned = CityPreferences.getPinnedCities(application)
        nonAdminCity = savedCity
        nonAdminPinnedCities = savedPinned

        val persistedAdmin = CityPreferences.isAdminSessionActive(application)
        val persistedLegacy = persistedAdmin && CityPreferences.isAdminLegacyFramelessActive(application)

        val initialCity = if (persistedLegacy) STRYI_CITY.copy(isPrimary = true) else savedCity
        val initialPinned = if (persistedLegacy) listOf(STRYI_CITY.copy(isPrimary = true)) else savedPinned

        val initialTempUnit = CityPreferences.getTempUnit(application)
        val initialBlockOrder = CityPreferences.getHomeBlockOrder(application)
        val initialUpdateNotice = CityPreferences.getReleaseUpdateNotice(application)
        val initialCustomization = CityPreferences.getCustomizationConfig(application)
        val languageOverride = CityPreferences.getAppLanguageOverride(application)
        val resolvedLanguage = languageOverride ?: AppLanguage.fromConfiguration(application)
        val showFirstLaunch = !CityPreferences.isFirstLaunchCompleted(application)
        val initialPro = CityPreferences.isProSubscribed(application)
        val initialVip = CityPreferences.isVipWhitelistedCache(application)

        _uiState.update {
            it.copy(
                selectedCity = initialCity,
                inspectedCity = initialCity,
                pinnedCities = initialPinned,
                isAdminMode = persistedAdmin,
                isAdminLegacyFrameless = persistedLegacy,
                isProSubscribed = initialPro,
                isVipWhitelisted = initialVip,
                settings = it.settings.copy(tempUnit = initialTempUnit),
                homeBlockOrder = initialBlockOrder,
                releaseUpdateNotice = initialUpdateNotice,
                customization = initialCustomization,
                debugSkyOverride = initialCustomization.savedSkyOverride,
                appLanguage = resolvedLanguage,
                appLanguageOverride = languageOverride,
                isFirstLaunchPickerVisible = showFirstLaunch
            )
        }
        loadWeather(initialCity)
        inspectCity(initialCity)
        startCloudReleaseObserver()
        firebaseAuth?.addAuthStateListener(authStateListener)
    }

    private fun startCloudReleaseObserver() {
        if (cloudReleaseJob?.isActive == true) return
        cloudReleaseJob = viewModelScope.launch {
            cloudReleaseRepository.observeLatestRelease()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    _uiState.update {
                        it.copy(
                            cloudSyncError = rootError.localizedMessage
                                ?: AppStrings.forLanguage(it.appLanguage).cloudSyncErrorFallback
                        )
                    }
                }
                .collect { cloudRelease ->
                    if (cloudRelease != null) {
                        val cloudNotice = ReleaseUpdateNotice(
                            isAvailable = cloudRelease.isAvailable,
                            targetVersion = cloudRelease.targetVersion.ifBlank {
                                suggestNextVersion(BuildConfig.VERSION_NAME)
                            },
                            releaseNotes = cloudRelease.releaseNotes
                        )
                        CityPreferences.saveReleaseUpdateNotice(getApplication(), cloudNotice)

                        val incomingAccent = AppAccentTheme.fromName(cloudRelease.accentTheme)
                        val updatedConfig = _uiState.value.customization.copy(
                            accentTheme = incomingAccent,
                            appTitle = cloudRelease.appTitle.ifBlank { _uiState.value.customization.appTitle }
                        )
                        CityPreferences.saveCustomizationConfig(getApplication(), updatedConfig)

                        _uiState.update {
                            it.copy(
                                releaseUpdateNotice = cloudNotice,
                                customization = updatedConfig,
                                cloudSyncError = null
                            )
                        }
                    }
                }
        }
    }

    private fun startOwnVipObserver() {
        stopOwnVipObserver()
        ownVipJob = viewModelScope.launch {
            cloudReleaseRepository.observeOwnVipStatus()
                .collect { isVip ->
                    CityPreferences.saveVipWhitelistedCache(getApplication(), isVip)
                    _uiState.update { it.copy(isVipWhitelisted = isVip) }
                }
        }
    }

    private fun stopOwnVipObserver() {
        ownVipJob?.cancel()
        ownVipJob = null
    }

    private fun startAdminVipListObserver() {
        if (firebaseAuth?.currentUser == null) return
        stopAdminVipListObserver()
        adminVipListJob = viewModelScope.launch {
            cloudReleaseRepository.observeAllVipEmails()
                .catch {
                    // Non-admin or offline: ignore gracefully
                }
                .collect { entries ->
                    _uiState.update { it.copy(vipEntries = entries) }
                }
        }
    }

    private fun stopAdminVipListObserver() {
        adminVipListJob?.cancel()
        adminVipListJob = null
    }

    private fun stopCloudReleaseObserver() {
        cloudReleaseJob?.cancel()
        cloudReleaseJob = null
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth?.removeAuthStateListener(authStateListener)
        stopCloudReleaseObserver()
        stopOwnVipObserver()
        stopAdminVipListObserver()
    }

    fun loadWeather(city: City = _uiState.value.selectedCity, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, selectedCity = city, errorMessage = null) }
            }

            val result = repository.getWeatherData(city)
            result.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        selectedCity = city,
                        weatherData = data,
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                try {
                    DannyWeatherWidgetProvider.updateAllWidgets(getApplication())
                } catch (_: Exception) {
                    // ignore
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = ex.localizedMessage
                            ?: AppStrings.forLanguage(it.appLanguage).weatherLoadErrorFallback
                    )
                }
            }
        }
    }

    fun selectCity(city: City, switchToWeatherTab: Boolean = false) {
        val state = _uiState.value
        val resolvedCity = if (state.isAdminMode && state.isAdminLegacyFrameless && city.id == STRYI_CITY.id) {
            city.copy(isPrimary = true)
        } else {
            city
        }
        if (!(state.isAdminMode && state.isAdminLegacyFrameless)) {
            CityPreferences.saveSelectedCity(getApplication(), resolvedCity)
            nonAdminCity = resolvedCity
        }
        _uiState.update {
            it.copy(
                isCityPickerVisible = false,
                searchQuery = "",
                searchResults = emptyList(),
                selectedTab = if (switchToWeatherTab) 0 else it.selectedTab
            )
        }
        loadWeather(resolvedCity)
        inspectCity(resolvedCity)
    }

    fun togglePinCity(city: City) {
        val state = _uiState.value
        val currentPinned = state.pinnedCities
        val exists = currentPinned.any { it.id == city.id }
        val updated = if (exists) {
            currentPinned.filterNot { it.id == city.id }
        } else {
            listOf(city) + currentPinned
        }
        if (!(state.isAdminMode && state.isAdminLegacyFrameless)) {
            CityPreferences.savePinnedCities(getApplication(), updated)
            nonAdminPinnedCities = updated
        }
        _uiState.update { it.copy(pinnedCities = updated) }
    }

    fun inspectCity(city: City) {
        articleJob?.cancel()
        val langCode = _uiState.value.appLanguage.code
        _uiState.update {
            it.copy(
                inspectedCity = city,
                isLoadingArticle = true,
                cityArticle = if (it.cityArticle?.city?.id == city.id) it.cityArticle else null
            )
        }
        articleJob = viewModelScope.launch {
            val article = repository.getCityInfo(city, langCode)
            _uiState.update {
                if (it.inspectedCity.id == city.id) {
                    it.copy(cityArticle = article, isLoadingArticle = false)
                } else {
                    it
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectWorldRegion(region: WorldRegion) {
        _uiState.update { it.copy(selectedWorldRegion = region) }
    }

    fun toggleCityPicker(show: Boolean) {
        _uiState.update {
            it.copy(
                isCityPickerVisible = show,
                searchQuery = if (!show) "" else it.searchQuery,
                searchResults = if (!show) emptyList() else it.searchResults
            )
        }
    }

    fun completeFirstLaunchWithCity(city: City) {
        CityPreferences.setFirstLaunchCompleted(getApplication(), true)
        _uiState.update {
            it.copy(
                isFirstLaunchPickerVisible = false,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
        }
        selectCity(city, switchToWeatherTab = true)
    }

    fun dismissFirstLaunchPicker() {
        CityPreferences.setFirstLaunchCompleted(getApplication(), true)
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                isFirstLaunchPickerVisible = false,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
        }
    }

    fun updateAppLanguage(override: AppLanguage?) {
        CityPreferences.saveAppLanguageMode(getApplication(), override)
        val resolved = override ?: AppLanguage.fromConfiguration(getApplication())
        _uiState.update {
            it.copy(
                appLanguage = resolved,
                appLanguageOverride = override
            )
        }
        inspectCity(_uiState.value.inspectedCity)
        try {
            DannyWeatherWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {
            // ignore
        }
    }

    fun syncSystemLanguage() {
        val currentState = _uiState.value
        if (currentState.appLanguageOverride != null) return
        val systemLang = AppLanguage.fromConfiguration(getApplication())
        if (currentState.appLanguage != systemLang) {
            _uiState.update { it.copy(appLanguage = systemLang) }
            inspectCity(currentState.inspectedCity)
            try {
                DannyWeatherWidgetProvider.updateAllWidgets(getApplication())
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchQuery = query, searchResults = emptyList(), isSearching = false) }
            return
        }
        val lang = _uiState.value.appLanguage
        val instantLocalMatches = filterCitiesLocally(
            cities = _uiState.value.presetCities,
            query = query,
            language = lang
        )
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = instantLocalMatches,
                isSearching = true
            )
        }
        searchJob = viewModelScope.launch {
            delay(220)
            val results = repository.searchCities(query, lang.code)
            _uiState.update { state ->
                if (state.searchQuery == query) {
                    state.copy(searchResults = results, isSearching = false)
                } else {
                    state
                }
            }
        }
    }

    fun updateTempUnit(unit: TemperatureUnit) {
        CityPreferences.saveTempUnit(getApplication(), unit)
        _uiState.update { it.copy(settings = it.settings.copy(tempUnit = unit)) }
        try {
            DannyWeatherWidgetProvider.updateAllWidgets(getApplication())
        } catch (_: Exception) {
            // ignore
        }
    }

    fun updatePressureUnit(unit: PressureUnit) {
        _uiState.update { it.copy(settings = it.settings.copy(pressureUnit = unit)) }
    }

    fun updateWindUnit(unit: WindUnit) {
        _uiState.update { it.copy(settings = it.settings.copy(windUnit = unit)) }
    }

    // Value formatting helpers
    fun formatTemp(tempC: Double): String {
        val unit = _uiState.value.settings.tempUnit
        return when (unit) {
            TemperatureUnit.CELSIUS -> {
                val round = Math.round(tempC).toInt()
                (if (round > 0) "+$round" else "$round") + "°"
            }
            TemperatureUnit.FAHRENHEIT -> {
                val tempF = (tempC * 9.0 / 5.0) + 32.0
                val round = Math.round(tempF).toInt()
                "$round°F"
            }
        }
    }

    fun formatPressure(pressureHpa: Double): String {
        val unit = _uiState.value.settings.pressureUnit
        val isUk = _uiState.value.appLanguage == AppLanguage.UKRAINIAN
        return when (unit) {
            PressureUnit.HPA -> if (isUk) "${Math.round(pressureHpa)} гПа" else "${Math.round(pressureHpa)} hPa"
            PressureUnit.MMHG -> {
                val mmHg = pressureHpa * 0.750062
                if (isUk) "${Math.round(mmHg)} мм рт. ст." else "${Math.round(mmHg)} mmHg"
            }
        }
    }

    fun formatWind(windKmh: Double): String {
        val unit = _uiState.value.settings.windUnit
        val isUk = _uiState.value.appLanguage == AppLanguage.UKRAINIAN
        return when (unit) {
            WindUnit.KMH -> if (isUk) "${String.format("%.1f", windKmh)} км/год" else "${String.format("%.1f", windKmh)} km/h"
            WindUnit.MS -> {
                val ms = windKmh / 3.6
                if (isUk) "${String.format("%.1f", ms)} м/с" else "${String.format("%.1f", ms)} m/s"
            }
        }
    }

    fun openSecretLogin() {
        _uiState.update {
            it.copy(
                isSecretLoginVisible = true,
                secretLoginError = null
            )
        }
    }

    fun dismissSecretLogin() {
        _uiState.update {
            it.copy(
                isSecretLoginVisible = false,
                secretLoginError = null
            )
        }
    }

    fun submitSecretCredentials(login: String, password: String) {
        if (SecretCredentials.isValid(login, password)) {
            val currentState = _uiState.value
            if (!currentState.isAdminMode) {
                nonAdminCity = currentState.selectedCity
                nonAdminPinnedCities = currentState.pinnedCities
            }
            CityPreferences.saveAdminSession(
                context = getApplication(),
                isAdmin = true,
                isLegacyFrameless = true
            )
            val adminPrimaryStryi = STRYI_CITY.copy(isPrimary = true)
            val adminPinned = listOf(adminPrimaryStryi)
            _uiState.update {
                it.copy(
                    isSecretLoginVisible = false,
                    isSecretMenuUnlocked = true,
                    isAdminMode = true,
                    isAdminLegacyFrameless = true,
                    pinnedCities = adminPinned,
                    secretLoginError = null,
                    secretStatusMessage = "Вхід виконано • Сесію адміністратора збережено"
                )
            }
            startAdminVipListObserver()
            loadWeather(adminPrimaryStryi)
            inspectCity(adminPrimaryStryi)
        } else {
            _uiState.update {
                it.copy(secretLoginError = "Невірний логін або пароль")
            }
            viewModelScope.launch {
                SecurityWebhookReporter.reportFailedAdminLogin(login)
            }
        }
    }

    fun toggleAdminLegacyFrameless(enabled: Boolean) {
        if (!_uiState.value.isAdminMode) return
        CityPreferences.saveAdminSession(
            context = getApplication(),
            isAdmin = true,
            isLegacyFrameless = enabled
        )
        if (enabled) {
            val adminPrimaryStryi = STRYI_CITY.copy(isPrimary = true)
            _uiState.update {
                it.copy(
                    isAdminLegacyFrameless = true,
                    pinnedCities = listOf(adminPrimaryStryi),
                    secretStatusMessage = "Активовано старшу версію: Стрий (без рамок)"
                )
            }
            loadWeather(adminPrimaryStryi)
            inspectCity(adminPrimaryStryi)
        } else {
            val restoredCity = nonAdminCity ?: CityPreferences.getSelectedCity(getApplication())
            val restoredPinned = nonAdminPinnedCities ?: CityPreferences.getPinnedCities(getApplication())
            _uiState.update {
                it.copy(
                    isAdminLegacyFrameless = false,
                    pinnedCities = restoredPinned,
                    secretStatusMessage = "Увімкнено стандартний інтерфейс v2.0"
                )
            }
            loadWeather(restoredCity)
            inspectCity(restoredCity)
        }
    }

    // --- Self-Serve Live Studio Customizations ---

    private fun applyCustomizationChange(
        changeSummaryUk: String,
        transform: (AppCustomizationConfig) -> AppCustomizationConfig
    ) {
        val updated = transform(_uiState.value.customization)
        CityPreferences.saveCustomizationConfig(getApplication(), updated)
        _uiState.update {
            it.copy(
                customization = updated,
                debugSkyOverride = updated.savedSkyOverride,
                hasUnpublishedAdminChanges = true,
                secretStatusMessage = changeSummaryUk
            )
        }
    }

    fun updateAppTitle(title: String) {
        val clean = title.take(28)
        applyCustomizationChange("Змінено назву на «${clean.ifBlank { "Danny Weather" }}»") {
            it.copy(appTitle = clean.ifBlank { "Danny Weather" })
        }
    }

    fun updateHeaderBadge(badge: String) {
        val clean = badge.trim().take(10)
        applyCustomizationChange(
            if (clean.isEmpty()) "Приховано бейдж шапки" else "Змінено бейдж на «$clean»"
        ) {
            it.copy(headerBadge = clean)
        }
    }

    fun updateVersionBannerText(text: String) {
        val clean = text.take(80)
        applyCustomizationChange("Оновлено текст версії") {
            it.copy(versionBannerText = clean.ifBlank { AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK })
        }
    }

    fun toggleHomeBlockVisibility(block: WeatherHomeBlock) {
        val currentHidden = _uiState.value.customization.hiddenHomeBlocks
        val willHide = block !in currentHidden
        if (willHide && currentHidden.size >= WeatherHomeBlock.entries.size - 1) {
            _uiState.update {
                it.copy(secretStatusMessage = "Має залишатися видимим хоча б один блок")
            }
            return
        }
        val nextHidden = if (willHide) currentHidden + block else currentHidden - block
        applyCustomizationChange(
            if (willHide) "Приховано блок «${block.titleUk}»" else "Увімкнено блок «${block.titleUk}»"
        ) {
            it.copy(hiddenHomeBlocks = nextHidden)
        }
    }

    fun toggleShowPinnedStrip(show: Boolean) {
        applyCustomizationChange(
            if (show) "Показано панель міст зверху" else "Приховано панель міст зверху"
        ) {
            it.copy(showPinnedStrip = show)
        }
    }

    fun toggleShowWorldCitiesTab(show: Boolean) {
        val currentlySelectedTab = _uiState.value.selectedTab
        applyCustomizationChange(
            if (show) "Увімкнено вкладку «Міста світу»" else "Приховано вкладку «Міста світу»"
        ) {
            it.copy(showWorldCitiesTab = show)
        }
        if (!show && currentlySelectedTab == 2) {
            _uiState.update { it.copy(selectedTab = 0) }
        }
    }

    fun setAccentTheme(theme: AppAccentTheme) {
        val updated = _uiState.value.customization.copy(accentTheme = theme)
        CityPreferences.saveCustomizationConfig(getApplication(), updated)
        _uiState.update {
            it.copy(
                customization = updated,
                secretStatusMessage = if (firebaseAuth?.currentUser != null) {
                    "Синхронізація кольору «${theme.labelUk}» у хмарі..."
                } else {
                    "Колір змінено локально на «${theme.labelUk}». Увійдіть через Google для синхронізації всім."
                }
            )
        }

        if (firebaseAuth?.currentUser != null) {
            viewModelScope.launch {
                cloudReleaseRepository.updateCloudAccentTheme(
                    accentTheme = theme,
                    fallbackVersion = _uiState.value.releaseUpdateNotice.targetVersion.ifBlank { BuildConfig.VERSION_NAME },
                    appTitle = updated.appTitle
                ).onSuccess {
                    _uiState.update {
                        it.copy(secretStatusMessage = "Колір «${theme.labelUk}» синхронізовано для всіх у реальному часі!")
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(secretStatusMessage = "Колір збережено локально: ${err.localizedMessage ?: "потрібен Google-акаунт адміна"}")
                    }
                }
            }
        }
    }

    fun setCardCornerRadius(radiusDp: Int) {
        val clamped = radiusDp.coerceIn(12, 36)
        applyCustomizationChange("Оновлено заокруглення карток ($clamped dp)") {
            it.copy(cardCornerRadiusDp = clamped)
        }
    }

    fun setGlassOpacity(level: Float) {
        val clamped = level.coerceIn(0.4f, 1.6f)
        val label = when {
            clamped <= 0.75f -> "Ультра-прозоре"
            clamped >= 1.25f -> "Насичене"
            else -> "Стандартне"
        }
        applyCustomizationChange("Змінено прозорість скла: $label") {
            it.copy(glassOpacityLevel = clamped)
        }
    }

    fun setAnimateLiquidBackground(animate: Boolean) {
        applyCustomizationChange(
            if (animate) "Увімкнено живу анімацію скла" else "Вимкнено анімацію фону (економія)"
        ) {
            it.copy(animateLiquidBackground = animate)
        }
    }

    fun setDefaultPresetCity(city: City) {
        CityPreferences.saveSelectedCity(getApplication(), city)
        _uiState.update {
            it.copy(
                selectedCity = city,
                inspectedCity = city,
                secretStatusMessage = "Стартове місто змінено на ${city.nameUk}"
            )
        }
        loadWeather(city)
        inspectCity(city)
    }

    fun resetAllCustomizations() {
        val defaultCustomization = AppCustomizationConfig()
        val defaultOrder = WeatherHomeBlock.DEFAULT_ORDER
        CityPreferences.saveCustomizationConfig(getApplication(), defaultCustomization)
        CityPreferences.saveHomeBlockOrder(getApplication(), defaultOrder)
        _uiState.update {
            it.copy(
                customization = defaultCustomization,
                homeBlockOrder = defaultOrder,
                debugSkyOverride = null,
                secretStatusMessage = "Усі налаштування скинуто до стандарту v2.0"
            )
        }
    }

    fun moveHomeBlock(index: Int, delta: Int) {
        val current = _uiState.value.homeBlockOrder
        val updated = swapHomeBlocks(current, index, delta)
        if (updated == current) return
        CityPreferences.saveHomeBlockOrder(getApplication(), updated)
        _uiState.update {
            it.copy(
                homeBlockOrder = updated,
                secretStatusMessage = "Порядок карток оновлено"
            )
        }
    }

    fun resetHomeBlockOrder() {
        val defaultOrder = WeatherHomeBlock.DEFAULT_ORDER
        CityPreferences.saveHomeBlockOrder(getApplication(), defaultOrder)
        _uiState.update {
            it.copy(
                homeBlockOrder = defaultOrder,
                secretStatusMessage = "Порядок блоків повернуто до стандартного"
            )
        }
    }

    fun publishUpdateNotice(version: String, notes: String) {
        val cleanVersion = version.trim().ifBlank { suggestNextVersion(BuildConfig.VERSION_NAME) }
        val cleanNotes = notes.trim().ifBlank { buildReleaseNotesFromOrder(_uiState.value.homeBlockOrder) }
        val notice = ReleaseUpdateNotice(
            isAvailable = true,
            targetVersion = cleanVersion,
            releaseNotes = cleanNotes
        )
        CityPreferences.saveReleaseUpdateNotice(getApplication(), notice)

        val currentUser = firebaseAuth?.currentUser
        if (currentUser == null) {
            _uiState.update {
                it.copy(
                    releaseUpdateNotice = notice,
                    hasUnpublishedAdminChanges = false,
                    secretStatusMessage = "Увійдіть через Google вище, щоб сповістити всіх користувачів через Firebase"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                releaseUpdateNotice = notice,
                hasUnpublishedAdminChanges = false,
                isCloudSyncing = true,
                cloudSyncError = null,
                secretStatusMessage = "Сповіщення про v$cleanVersion надсилається у Firebase..."
            )
        }

        viewModelScope.launch {
            val result = cloudReleaseRepository.publishRelease(
                targetVersion = cleanVersion,
                releaseNotes = cleanNotes,
                appTitle = _uiState.value.customization.appTitle,
                accentTheme = _uiState.value.customization.accentTheme
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isCloudSyncing = false,
                        cloudSyncError = null,
                        secretStatusMessage = "Готово! Усі користувачі отримали сповіщення про версію v$cleanVersion"
                    )
                }
            }.onFailure { err ->
                val isPermissionDenied = generateSequence(err) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .any { it.code == FirebaseFirestoreException.Code.PERMISSION_DENIED }
                val message = if (isPermissionDenied) {
                    "Доступ до хмарного запису має лише адмін-акаунт Google"
                } else {
                    "Не вдалося зберегти у хмару: ${err.localizedMessage ?: "перевірте мережу"}"
                }
                _uiState.update {
                    it.copy(
                        isCloudSyncing = false,
                        cloudSyncError = message,
                        secretStatusMessage = message
                    )
                }
            }
        }
    }

    fun dismissUpdateBannerLocally() {
        val hiddenNotice = _uiState.value.releaseUpdateNotice.copy(isAvailable = false)
        CityPreferences.saveReleaseUpdateNotice(getApplication(), hiddenNotice)
        _uiState.update {
            it.copy(
                releaseUpdateNotice = hiddenNotice,
                isAccountUpdateBannerDismissed = true,
                hasUnpublishedAdminChanges = false
            )
        }
    }

    fun triggerManualAccountUpdateNotice() {
        _uiState.update {
            it.copy(
                isAccountUpdateBannerDismissed = false,
                pendingConnectedAccountNotice = true
            )
        }
    }

    fun consumePendingConnectedAccountNotice() {
        _uiState.update { it.copy(pendingConnectedAccountNotice = false) }
    }

    fun clearUpdateNotice() {
        dismissUpdateBannerLocally()
        if (firebaseAuth?.currentUser != null) {
            viewModelScope.launch {
                cloudReleaseRepository.hideLatestRelease()
                    .onSuccess {
                        _uiState.update {
                            it.copy(secretStatusMessage = "Сповіщення про оновлення приховано для всіх")
                        }
                    }
            }
        }
    }

    fun addVipEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return
        if (firebaseAuth?.currentUser == null) {
            _uiState.update {
                it.copy(secretStatusMessage = "Увійдіть через Google, щоб додати VIP-доступ")
            }
            return
        }
        viewModelScope.launch {
            cloudReleaseRepository.addVipEmail(trimmed)
                .onSuccess { added ->
                    _uiState.update {
                        it.copy(secretStatusMessage = "Додано у VIP-доступ: ${added.email}")
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(secretStatusMessage = err.localizedMessage ?: "Не вдалося додати email")
                    }
                }
        }
    }

    fun removeVipEmail(emailOrDocId: String) {
        if (firebaseAuth?.currentUser == null) return
        viewModelScope.launch {
            cloudReleaseRepository.removeVipEmail(emailOrDocId)
                .onSuccess {
                    _uiState.update {
                        it.copy(secretStatusMessage = "Email видалено зі списку VIP-доступу")
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(secretStatusMessage = "Помилка видалення: ${err.localizedMessage ?: "доступ обмежено"}")
                    }
                }
        }
    }

    fun setStatusMessage(message: String) {
        _uiState.update { it.copy(secretStatusMessage = message) }
    }

    fun repairAndRefreshWidget() {
        try {
            DannyWeatherWidgetProvider.updateAllWidgets(getApplication())
            _uiState.update {
                it.copy(secretStatusMessage = "Віджети перезапущено та синхронізовано з Open-Meteo")
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(secretStatusMessage = "Помилка оновлення віджета: ${e.localizedMessage ?: "невідома"}")
            }
        }
    }

    fun logoutAdminMode() {
        CityPreferences.saveAdminSession(
            context = getApplication(),
            isAdmin = false,
            isLegacyFrameless = false
        )
        stopAdminVipListObserver()
        val restoredCity = nonAdminCity ?: CityPreferences.getSelectedCity(getApplication())
        val restoredPinned = nonAdminPinnedCities ?: CityPreferences.getPinnedCities(getApplication())
        _uiState.update {
            it.copy(
                isAdminMode = false,
                isAdminLegacyFrameless = false,
                isSecretMenuUnlocked = false,
                vipEntries = emptyList(),
                debugSkyOverride = it.customization.savedSkyOverride,
                secretStatusMessage = null,
                pinnedCities = restoredPinned
            )
        }
        loadWeather(restoredCity)
        inspectCity(restoredCity)
    }

    fun reopenSecretMenuIfAdmin() {
        if (!_uiState.value.isAdminMode) return
        startAdminVipListObserver()
        _uiState.update {
            it.copy(
                isSecretMenuUnlocked = true,
                secretStatusMessage = null
            )
        }
    }

    fun dismissSecretMenu() {
        _uiState.update {
            it.copy(
                isSecretMenuUnlocked = false,
                secretStatusMessage = null
            )
        }
    }

    fun setDebugSkyOverride(skyType: SkyType?) {
        val updatedConfig = _uiState.value.customization.copy(savedSkyOverride = skyType)
        CityPreferences.saveCustomizationConfig(getApplication(), updatedConfig)
        val label = if (skyType == null) {
            "Атмосфера повернута до реальної погоди"
        } else {
            "Постійний фон неба: ${skyType.name}"
        }
        _uiState.update {
            it.copy(
                customization = updatedConfig,
                debugSkyOverride = skyType,
                secretStatusMessage = label
            )
        }
    }

    fun forceRefreshFromSecretMenu() {
        loadWeather(isRefresh = true)
        _uiState.update {
            it.copy(secretStatusMessage = "Запущено примусове оновлення погоди та віджета")
        }
    }

    fun clearWeatherCacheFromSecretMenu() {
        viewModelScope.launch {
            try {
                repository.clearCache()
                _uiState.update {
                    it.copy(secretStatusMessage = "Локальний кеш бази даних очищено")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(secretStatusMessage = "Не вдалося очистити кеш")
                }
            }
        }
    }

    companion object {
        fun swapHomeBlocks(
            order: List<WeatherHomeBlock>,
            index: Int,
            delta: Int
        ): List<WeatherHomeBlock> {
            val target = index + delta
            if (index !in order.indices || target !in order.indices || index == target) return order
            return order.toMutableList().apply {
                val moved = this[index]
                this[index] = this[target]
                this[target] = moved
            }
        }

        fun suggestNextVersion(current: String): String {
            val numeric = Regex("""(\d+)\.(\d+)""").find(current)
            if (numeric != null) {
                val major = numeric.groupValues[1].toIntOrNull() ?: 2
                val minor = numeric.groupValues[2].toIntOrNull() ?: 0
                return "$major.${minor + 1}"
            }
            return "2.1"
        }

        fun isCloudVersionNewer(cloudVersion: String, installedVersion: String): Boolean {
            val cloudParts = parseVersionParts(cloudVersion)
            val installedParts = parseVersionParts(installedVersion)
            if (cloudParts.isEmpty() || installedParts.isEmpty()) return false
            val maxLen = maxOf(cloudParts.size, installedParts.size)
            for (i in 0 until maxLen) {
                val c = cloudParts.getOrElse(i) { 0 }
                val inst = installedParts.getOrElse(i) { 0 }
                if (c != inst) return c > inst
            }
            return false
        }

        private fun parseVersionParts(raw: String): List<Int> {
            val clean = Regex("""\d+(?:\.\d+)*""").find(raw.trim())?.value ?: return emptyList()
            return clean.split(".").mapNotNull { it.toIntOrNull() }
        }

        fun buildReleaseNotesFromOrder(order: List<WeatherHomeBlock>): String {
            val blocksLabel = order.joinToString(" → ") { it.titleUk }
            return "Оновлено порядок карток ($blocksLabel), покращено стабільність віджетів головного екрана та продуктивність Danny Weather."
        }
    }
}
