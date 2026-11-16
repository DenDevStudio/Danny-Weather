package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import com.example.BuildConfig
import com.example.auth.GoogleAuthHelper
import com.example.util.UpdateNotificationHelper
import com.example.data.model.City
import com.example.data.model.ReleaseUpdateNotice
import com.example.data.model.SkyType
import com.example.data.model.WeatherHomeBlock
import com.example.ui.components.AirQualityCard
import com.example.ui.components.CitySelectorSheet
import com.example.ui.components.CurrentWeatherCard
import com.example.ui.components.DailyForecastSection
import com.example.ui.components.FirstLaunchCityDialog
import com.example.ui.components.HourlyForecastSection
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LocalAnimateLiquidBackground
import com.example.ui.components.LocalAppAccentColor
import com.example.ui.components.LocalCardCornerRadius
import com.example.ui.components.LocalFramelessAdminMode
import com.example.ui.components.LocalGlassOpacity
import com.example.ui.components.SecretDeveloperMenuDialog
import com.example.ui.components.SecretLoginDialog
import com.example.ui.components.SettingsSection
import com.example.ui.components.WeatherBackground
import com.example.ui.components.WeatherMetricsGrid
import com.example.ui.components.WorldCitiesSection
import com.example.ui.components.glassBorder
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedSubtitle
import com.example.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun openPlayStoreListing(context: android.content.Context, errorMessage: String) {
    val pkg = BuildConfig.APPLICATION_ID
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(marketIntent)
    } catch (_: Exception) {
        try {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isUpdateSheetVisible by remember { mutableStateOf(false) }
    var lastAutoShownVersion by remember { mutableStateOf<String?>(null) }

    val activeStrings = remember(uiState.appLanguage) {
        AppStrings.forLanguage(uiState.appLanguage)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val targetVer = uiState.releaseUpdateNotice.targetVersion.ifBlank { BuildConfig.VERSION_NAME }
            UpdateNotificationHelper.showAccountUpdateNotification(
                context = context,
                strings = activeStrings,
                targetVersion = targetVer
            )
        }
    }

    LaunchedEffect(configuration) {
        viewModel.syncSystemLanguage()
    }

    LaunchedEffect(Unit) {
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    val isNewCloudReleaseAvailable = remember(
        uiState.releaseUpdateNotice.isAvailable,
        uiState.releaseUpdateNotice.targetVersion
    ) {
        uiState.releaseUpdateNotice.isAvailable &&
            WeatherViewModel.isCloudVersionNewer(
                uiState.releaseUpdateNotice.targetVersion,
                BuildConfig.VERSION_NAME
            )
    }

    LaunchedEffect(
        isNewCloudReleaseAvailable,
        uiState.releaseUpdateNotice.targetVersion,
        uiState.isSecretMenuUnlocked,
        uiState.isFirstLaunchPickerVisible
    ) {
        val version = uiState.releaseUpdateNotice.targetVersion
        if (
            isNewCloudReleaseAvailable &&
            !uiState.isSecretMenuUnlocked &&
            !uiState.isFirstLaunchPickerVisible &&
            lastAutoShownVersion != version
        ) {
            lastAutoShownVersion = version
            if (uiState.isGoogleSignedIn) {
                if (UpdateNotificationHelper.canPostNotifications(context)) {
                    UpdateNotificationHelper.showAccountUpdateNotification(
                        context = context,
                        strings = activeStrings,
                        targetVersion = version
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            isUpdateSheetVisible = true
        }
    }

    val triggerGoogleSignIn: () -> Unit = {
        GoogleAuthHelper.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                viewModel.setStatusMessage(activeStrings.googleConnectedStatus)
                Toast.makeText(context, activeStrings.googleSignInSuccessToast, Toast.LENGTH_SHORT).show()
            },
            onAuthError = { msg ->
                viewModel.setStatusMessage(msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            scope = coroutineScope,
            onAuthCancelled = {},
            language = uiState.appLanguage
        )
    }

    val triggerGoogleSignOut: () -> Unit = {
        GoogleAuthHelper.signOut(
            context = context,
            credentialManager = credentialManager,
            scope = coroutineScope,
            onSignOutComplete = {
                viewModel.setStatusMessage(activeStrings.googleSignedOutStatus)
            }
        )
    }

    val currentSkyType = uiState.debugSkyOverride ?: uiState.weatherData?.current?.condition?.skyType ?: SkyType.CLEAR_DAY
    val isFramelessAdmin = uiState.isAdminMode && uiState.isAdminLegacyFrameless
    val customAccent = Color(uiState.customization.accentTheme.colorHex)

    CompositionLocalProvider(
        LocalFramelessAdminMode provides isFramelessAdmin,
        LocalAppAccentColor provides customAccent,
        LocalCardCornerRadius provides uiState.customization.cardCornerRadiusDp.dp,
        LocalGlassOpacity provides uiState.customization.glassOpacityLevel,
        LocalAnimateLiquidBackground provides uiState.customization.animateLiquidBackground,
        LocalAppStrings provides activeStrings
    ) {
        WeatherBackground(
            skyType = currentSkyType,
            modifier = modifier
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    DannyWeatherBottomBar(
                        selectedTab = uiState.selectedTab,
                        showWorldTab = uiState.customization.showWorldCitiesTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Adaptive max width for tablet/landscape support
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 640.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top App Header with custom title, badge (non-admin only) & pinned cities strip
                        TopHeaderBar(
                            appName = uiState.customization.appTitle,
                            headerBadge = uiState.customization.headerBadge,
                            showPinnedStrip = uiState.customization.showPinnedStrip,
                            cityName = uiState.selectedCity.localizedName(uiState.appLanguage),
                            selectedCity = uiState.selectedCity,
                            pinnedCities = uiState.pinnedCities,
                            isAdminMode = uiState.isAdminMode,
                            onAdminClick = { viewModel.reopenSecretMenuIfAdmin() },
                            onSelectCity = { viewModel.selectCity(it) },
                            onTogglePinCity = { viewModel.togglePinCity(it) },
                            onCityClick = { viewModel.toggleCityPicker(true) }
                        )

                        // Body content according to state
                        when {
                            uiState.isLoading && uiState.weatherData == null -> {
                                LoadingStateView(cityName = uiState.selectedCity.localizedName(uiState.appLanguage))
                            }
                            uiState.errorMessage != null && uiState.weatherData == null -> {
                                ErrorStateView(
                                    error = uiState.errorMessage ?: activeStrings.errorWeatherTitle,
                                    onRetry = { viewModel.loadWeather() }
                                )
                            }
                            uiState.weatherData != null -> {
                                val data = uiState.weatherData!!
                                val todayForecast = data.daily.firstOrNull()
                                val visibleBlocks = remember(
                                    uiState.homeBlockOrder,
                                    uiState.customization.hiddenHomeBlocks
                                ) {
                                    uiState.homeBlockOrder.filterNot {
                                        it in uiState.customization.hiddenHomeBlocks
                                    }
                                }

                                AnimatedContent(
                                    targetState = uiState.selectedTab,
                                    transitionSpec = {
                                        val fadeSpec = tween<Float>(210, easing = FastOutSlowInEasing)
                                        val slideInSpec = tween<IntOffset>(240, easing = FastOutSlowInEasing)
                                        val slideOutSpec = tween<IntOffset>(180, easing = FastOutSlowInEasing)
                                        if (targetState > initialState) {
                                            (slideInHorizontally(slideInSpec) { it / 5 } + fadeIn(fadeSpec))
                                                .togetherWith(slideOutHorizontally(slideOutSpec) { -it / 5 } + fadeOut(fadeSpec))
                                        } else {
                                            (slideInHorizontally(slideInSpec) { -it / 5 } + fadeIn(fadeSpec))
                                                .togetherWith(slideOutHorizontally(slideOutSpec) { it / 5 } + fadeOut(fadeSpec))
                                        }
                                    },
                                    label = "tab_smooth_transition",
                                    modifier = Modifier.fillMaxSize()
                                ) { currentTab ->
                                    when (currentTab) {
                                        0 -> {
                                            // "Сьогодні" Tab: Update Banner + Configurable Visible Blocks
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                item(key = "app_update_banner", contentType = "banner") {
                                                    AnimatedVisibility(
                                                        visible = isNewCloudReleaseAvailable,
                                                        enter = fadeIn() + expandVertically(),
                                                        exit = fadeOut() + shrinkVertically()
                                                    ) {
                                                        AppUpdateBannerCard(
                                                            notice = uiState.releaseUpdateNotice,
                                                            isNewRelease = isNewCloudReleaseAvailable,
                                                            onUpdateClick = {
                                                                openPlayStoreListing(context, activeStrings.playStoreOpenError)
                                                            },
                                                            onDismissClick = { viewModel.dismissUpdateBannerLocally() }
                                                        )
                                                    }
                                                }

                                                items(
                                                    items = visibleBlocks,
                                                    key = { it.name },
                                                    contentType = { "home_block_${it.name}" }
                                                ) { block ->
                                                    Box(modifier = Modifier.animateItem()) {
                                                        when (block) {
                                                            WeatherHomeBlock.CURRENT -> {
                                                                CurrentWeatherCard(
                                                                    city = data.city,
                                                                    current = data.current,
                                                                    todayForecast = todayForecast,
                                                                    formattedTemp = viewModel.formatTemp(data.current.tempCelsius),
                                                                    formattedFeelsLike = viewModel.formatTemp(data.current.feelsLikeCelsius),
                                                                    isRefreshing = uiState.isRefreshing,
                                                                    onCityClick = { viewModel.toggleCityPicker(true) },
                                                                    onRefreshClick = { viewModel.loadWeather(isRefresh = true) }
                                                                )
                                                            }
                                                            WeatherHomeBlock.HOURLY -> {
                                                                HourlyForecastSection(
                                                                    hourlyItems = data.hourly,
                                                                    formatTemp = { viewModel.formatTemp(it) }
                                                                )
                                                            }
                                                            WeatherHomeBlock.METRICS -> {
                                                                WeatherMetricsGrid(
                                                                    current = data.current,
                                                                    todayForecast = todayForecast,
                                                                    formatWind = { viewModel.formatWind(it) },
                                                                    formatPressure = { viewModel.formatPressure(it) }
                                                                )
                                                            }
                                                            WeatherHomeBlock.AIR_QUALITY -> {
                                                                AirQualityCard(airQuality = data.airQuality)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        1 -> {
                                            // "14 Днів" Tab: Full 14-day forecast + PRO gating + honest accuracy disclaimer
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                item(key = "daily_forecast_section", contentType = "section") {
                                                    DailyForecastSection(
                                                        cityName = data.city.localizedName(uiState.appLanguage),
                                                        dailyItems = data.daily,
                                                        formatTemp = { viewModel.formatTemp(it) },
                                                        isProUnlocked = uiState.isProAccessGranted,
                                                        onOpenProUpgrade = {
                                                            openPlayStoreListing(context, activeStrings.playStoreOpenError)
                                                        }
                                                    )
                                                }

                                                item(key = "daily_air_quality_card", contentType = "card") {
                                                    AirQualityCard(airQuality = data.airQuality)
                                                }
                                            }
                                        }

                                        2 -> {
                                            // World Cities Tab
                                            val citiesListState = rememberLazyListState()
                                            val coroutineScope = rememberCoroutineScope()

                                            WorldCitiesSection(
                                                selectedCity = uiState.selectedCity,
                                                inspectedCity = uiState.inspectedCity,
                                                cityArticle = uiState.cityArticle,
                                                isLoadingArticle = uiState.isLoadingArticle,
                                                pinnedCities = uiState.pinnedCities,
                                                presetCities = uiState.presetCities,
                                                selectedRegion = uiState.selectedWorldRegion,
                                                searchQuery = uiState.searchQuery,
                                                searchResults = uiState.searchResults,
                                                isSearching = uiState.isSearching,
                                                isAdminLegacyMode = isFramelessAdmin,
                                                listState = citiesListState,
                                                onRegionSelected = { viewModel.selectWorldRegion(it) },
                                                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                                onInspectCity = { city ->
                                                    viewModel.inspectCity(city)
                                                    coroutineScope.launch {
                                                        citiesListState.animateScrollToItem(if (isFramelessAdmin) 2 else 1)
                                                    }
                                                },
                                                onSelectCityWeather = { viewModel.selectCity(it, switchToWeatherTab = true) },
                                                onTogglePinCity = { viewModel.togglePinCity(it) }
                                            )
                                        }

                                        3 -> {
                                            // Settings Tab
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                item(key = "settings_section", contentType = "card") {
                                                    SettingsSection(
                                                        settings = uiState.settings,
                                                        onTempUnitChange = { viewModel.updateTempUnit(it) },
                                                        onPressureUnitChange = { viewModel.updatePressureUnit(it) },
                                                        onWindUnitChange = { viewModel.updateWindUnit(it) },
                                                        appLanguageOverride = uiState.appLanguageOverride,
                                                        onAppLanguageChange = { viewModel.updateAppLanguage(it) },
                                                        onOpenSecretLogin = { viewModel.openSecretLogin() },
                                                        versionBannerText = uiState.customization.versionBannerText,
                                                        isGoogleSignedIn = uiState.isGoogleSignedIn,
                                                        cloudUserEmail = uiState.cloudUserEmail,
                                                        isProUnlocked = uiState.isProAccessGranted,
                                                        isVipWhitelisted = uiState.isVipWhitelisted,
                                                        isAdminMode = uiState.isAdminMode,
                                                        onOpenProUpgrade = {
                                                            openPlayStoreListing(context, activeStrings.playStoreOpenError)
                                                        },
                                                        onSignInWithGoogle = triggerGoogleSignIn,
                                                        onSignOutGoogle = triggerGoogleSignOut
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // First-launch onboarding: prompt for city or country with explicit close ("X") button
        if (uiState.isFirstLaunchPickerVisible) {
            FirstLaunchCityDialog(
                selectedCity = uiState.selectedCity,
                presetCities = uiState.presetCities,
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onCitySelected = { viewModel.completeFirstLaunchWithCity(it) },
                onDismiss = { viewModel.dismissFirstLaunchPicker() }
            )
        }

        // City Selector Bottom Sheet
        if (uiState.isCityPickerVisible) {
            CitySelectorSheet(
                sheetState = sheetState,
                selectedCity = uiState.selectedCity,
                pinnedCities = uiState.pinnedCities,
                presetCities = uiState.presetCities,
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onCitySelected = { viewModel.selectCity(it) },
                onTogglePinCity = { viewModel.togglePinCity(it) },
                onDismiss = { viewModel.toggleCityPicker(false) }
            )
        }

        if (isUpdateSheetVisible) {
            UpdateDistributionDialog(
                notice = uiState.releaseUpdateNotice,
                isNewRelease = isNewCloudReleaseAvailable,
                onUpdatePlayStore = {
                    isUpdateSheetVisible = false
                    openPlayStoreListing(context, activeStrings.playStoreOpenError)
                },
                onDismiss = { isUpdateSheetVisible = false }
            )
        }

        if (uiState.isSecretLoginVisible) {
            SecretLoginDialog(
                errorMessage = uiState.secretLoginError,
                onSubmit = { login, password -> viewModel.submitSecretCredentials(login, password) },
                onDismiss = { viewModel.dismissSecretLogin() }
            )
        }

        if (uiState.isSecretMenuUnlocked) {
            SecretDeveloperMenuDialog(
                selectedCity = uiState.selectedCity,
                pinnedCitiesCount = uiState.pinnedCities.size,
                weatherData = uiState.weatherData,
                isAdminLegacyFrameless = uiState.isAdminLegacyFrameless,
                debugSkyOverride = uiState.debugSkyOverride,
                homeBlockOrder = uiState.homeBlockOrder,
                customization = uiState.customization,
                releaseUpdateNotice = uiState.releaseUpdateNotice,
                statusMessage = uiState.secretStatusMessage,
                isGoogleSignedIn = uiState.isGoogleSignedIn,
                cloudUserEmail = uiState.cloudUserEmail,
                isCloudSyncing = uiState.isCloudSyncing,
                vipEntries = uiState.vipEntries,
                onAddVipEmail = { viewModel.addVipEmail(it) },
                onRemoveVipEmail = { viewModel.removeVipEmail(it) },
                onSignInWithGoogle = triggerGoogleSignIn,
                onToggleAdminLegacyFrameless = { viewModel.toggleAdminLegacyFrameless(it) },
                onUpdateAppTitle = { viewModel.updateAppTitle(it) },
                onUpdateHeaderBadge = { viewModel.updateHeaderBadge(it) },
                onUpdateVersionBannerText = { viewModel.updateVersionBannerText(it) },
                onMoveHomeBlock = { index, delta -> viewModel.moveHomeBlock(index, delta) },
                onToggleHomeBlockVisibility = { viewModel.toggleHomeBlockVisibility(it) },
                onResetHomeBlockOrder = { viewModel.resetHomeBlockOrder() },
                onToggleShowPinnedStrip = { viewModel.toggleShowPinnedStrip(it) },
                onToggleShowWorldCitiesTab = { viewModel.toggleShowWorldCitiesTab(it) },
                onSetAccentTheme = { viewModel.setAccentTheme(it) },
                onSetCardCornerRadius = { viewModel.setCardCornerRadius(it) },
                onSetGlassOpacity = { viewModel.setGlassOpacity(it) },
                onSetAnimateLiquidBackground = { viewModel.setAnimateLiquidBackground(it) },
                onSetDefaultPresetCity = { viewModel.setDefaultPresetCity(it) },
                onResetAllCustomizations = { viewModel.resetAllCustomizations() },
                onPublishUpdateNotice = { version, notes -> viewModel.publishUpdateNotice(version, notes) },
                onClearUpdateNotice = { viewModel.clearUpdateNotice() },
                onRepairWidget = { viewModel.repairAndRefreshWidget() },
                onSelectSkyOverride = { viewModel.setDebugSkyOverride(it) },
                onLogoutAdmin = { viewModel.logoutAdminMode() },
                onDismiss = { viewModel.dismissSecretMenu() }
            )
        }
    }
}

@Composable
private fun UpdateDistributionDialog(
    notice: ReleaseUpdateNotice,
    isNewRelease: Boolean,
    onUpdatePlayStore: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val accentColor = LocalAppAccentColor.current
    val playUrl = UpdateNotificationHelper.playMarketWebUrl()
    val dialogTitle = if (isNewRelease) {
        "${strings.updateAvailablePrefix} v${notice.targetVersion}"
    } else {
        strings.accountUpdateTitle
    }
    val dialogSubtitle = if (isNewRelease) {
        strings.updateDialogSubtitle
    } else {
        strings.accountUpdateSubtitle
    }
    val dialogBody = if (isNewRelease && notice.releaseNotes.isNotBlank()) {
        notice.releaseNotes
    } else {
        strings.accountUpdateBody
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xF0102544), Color(0xF50A172E))
                    )
                )
                .glassBorder(1.2.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(26.dp), frameless = false)
                .padding(20.dp)
                .testTag("update_distribution_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dialogTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = dialogSubtitle,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.closeCd,
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Text(
                    text = dialogBody,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )

                // Visible clickable Google Play Market URL link
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1E38BDF8))
                        .clickable(onClick = onUpdatePlayStore)
                        .padding(12.dp)
                        .testTag("play_market_link_box"),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.playMarketLinkLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Text(
                        text = playUrl,
                        fontSize = 11.sp,
                        color = Color(0xFFBAE6FD),
                        textDecoration = TextDecoration.Underline,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.85f)
                    ) {
                        Text(
                            text = strings.laterLabel,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onUpdatePlayStore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("play_store_update_confirm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.openPlayMarketButton,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUpdateBannerCard(
    notice: ReleaseUpdateNotice,
    isNewRelease: Boolean,
    onUpdateClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    val playUrl = UpdateNotificationHelper.playMarketWebUrl()
    val bannerTitle = if (isNewRelease) {
        "${strings.updateAvailablePrefix} v${notice.targetVersion}"
    } else {
        strings.accountUpdateTitle
    }
    val bannerSubtitle = if (isNewRelease) strings.updateBannerSubtitle else strings.accountUpdateSubtitle
    val bannerBody = if (isNewRelease && notice.releaseNotes.isNotBlank()) {
        notice.releaseNotes
    } else {
        strings.accountUpdateBody
    }

    LiquidGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .testTag("play_update_banner"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(accentColor.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NewReleases,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = bannerTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = bannerSubtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }
            }

            Text(
                text = bannerBody,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1C38BDF8))
                    .clickable(onClick = onUpdateClick)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("banner_play_market_link"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = playUrl,
                    fontSize = 11.sp,
                    color = Color(0xFFBAE6FD),
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUpdateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("play_update_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.openPlayMarketButton,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = onDismissClick,
                    modifier = Modifier.testTag("play_update_later_button")
                ) {
                    Text(
                        text = strings.laterLabel,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeaderBar(
    appName: String,
    headerBadge: String,
    showPinnedStrip: Boolean,
    cityName: String,
    selectedCity: City,
    pinnedCities: List<City>,
    isAdminMode: Boolean,
    onAdminClick: () -> Unit,
    onSelectCity: (City) -> Unit,
    onTogglePinCity: (City) -> Unit,
    onCityClick: () -> Unit
) {
    val frameless = LocalFramelessAdminMode.current
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    val lang = strings.language
    val isCurrentPinned = remember(pinnedCities, selectedCity) {
        pinnedCities.any { it.id == selectedCity.id }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
                    .testTag("app_brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = appName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Show version badge (e.g. v2.0) to regular users; hide it when logged into admin mode
                        if (!isAdminMode && headerBadge.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x3338BDF8))
                                    .glassBorder(0.8.dp, Color(0x6638BDF8), RoundedCornerShape(8.dp), frameless)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                    .testTag("header_version_badge")
                            ) {
                                Text(
                                    text = headerBadge,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF67E8F9)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (selectedCity.isPrimary) {
                            "${selectedCity.flagEmoji} $cityName • ${strings.primaryCityBadge}"
                        } else {
                            "${selectedCity.flagEmoji} $cityName"
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdminMode) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onAdminClick)
                            .testTag("top_admin_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .glassBorder(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(11.dp), frameless),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeveloperMode,
                                contentDescription = "DenDev Studio",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick Pin/Unpin button for active city
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTogglePinCity(selectedCity) }
                        .testTag("top_pin_current_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isCurrentPinned) accentColor.copy(alpha = 0.22f) else Color(0x22FFFFFF))
                            .glassBorder(
                                1.dp,
                                if (isCurrentPinned) accentColor else Color(0x40FFFFFF),
                                RoundedCornerShape(11.dp),
                                frameless
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCurrentPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isCurrentPinned) strings.unpinTooltip else strings.pinTooltip,
                            tint = if (isCurrentPinned) accentColor else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (showPinnedStrip) {
            Spacer(modifier = Modifier.height(10.dp))

            // Pinned Cities Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCurrentPinned) {
                    QuickCityLiquidPill(
                        title = "${selectedCity.flagEmoji} ${selectedCity.localizedName(lang)}",
                        subtitle = strings.tapPinHint,
                        isSelected = true,
                        isPinned = false,
                        onClick = { onSelectCity(selectedCity) },
                        onPinAction = { onTogglePinCity(selectedCity) }
                    )
                }

                pinnedCities.forEach { city ->
                    QuickCityLiquidPill(
                        title = "${city.flagEmoji} ${city.localizedName(lang)}",
                        subtitle = if (city.isPrimary) strings.primaryCityBadge else city.localizedSubtitle(lang),
                        isSelected = selectedCity.id == city.id,
                        isPinned = true,
                        onClick = { onSelectCity(city) },
                        onPinAction = { onTogglePinCity(city) }
                    )
                }

                // Quick "+ Pin" action pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1EFFFFFF))
                        .glassBorder(1.dp, Color(0x55FFFFFF), RoundedCornerShape(16.dp), frameless)
                        .clickable(onClick = onCityClick)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("top_add_pin_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.pinActionLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCityLiquidPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onPinAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAppAccentColor.current
    val pillBorder = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(accentColor, accentColor.copy(alpha = 0.7f), Color(0x88FFFFFF))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0x55FFFFFF), Color(0x22FFFFFF))
        )
    }

    val pillBackground = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0.1f))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0x20FFFFFF), Color(0x10FFFFFF))
        )
    }

    val frameless = LocalFramelessAdminMode.current
    val strings = LocalAppStrings.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(brush = pillBackground)
            .glassBorder(1.2.dp, brush = pillBorder, shape = RoundedCornerShape(16.dp), frameless = frameless)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 6.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) accentColor else Color.White
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF))
                .clickable(onClick = onPinAction),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPinned) Icons.Rounded.Close else Icons.Outlined.PushPin,
                contentDescription = if (isPinned) strings.unpinTooltip else strings.pinTooltip,
                tint = if (isPinned) Color.White.copy(alpha = 0.75f) else accentColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun DannyWeatherBottomBar(
    selectedTab: Int,
    showWorldTab: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    val tabs = remember(showWorldTab, strings) {
        buildList {
            add(Triple(0, strings.tabToday, Icons.Rounded.Cloud))
            add(Triple(1, strings.tabSevenDays, Icons.Rounded.CalendarMonth))
            if (showWorldTab) {
                add(Triple(2, strings.tabWorldCities, Icons.Rounded.Public))
            }
            add(Triple(3, strings.tabSettings, Icons.Rounded.Settings))
        }
    }

    val visualIndex = remember(tabs, selectedTab) {
        tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    }

    // Read animatedTabOffsetState ONLY inside Modifier.offset { } to avoid recomposing the bar every frame
    val animatedTabOffsetState = animateFloatAsState(
        targetValue = visualIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabGlideSpring"
    )

    val glassFill = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x520D2D59),
                Color(0x660A2245),
                Color(0x80081833)
            )
        )
    }

    val topBorder = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x22FFFFFF),
                Color(0x99FFFFFF),
                Color(0x44FFFFFF),
                Color(0x99FFFFFF),
                Color(0x22FFFFFF)
            )
        )
    }

    val topHighlightLine = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x88FFFFFF),
                Color(0xEEFFFFFF),
                Color(0x88FFFFFF),
                Color.Transparent
            )
        )
    }

    val activePillFill = remember(accentColor) {
        Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.24f),
                accentColor.copy(alpha = 0.10f)
            )
        )
    }

    val activePillBorder = remember(accentColor) {
        Brush.linearGradient(
            colors = listOf(accentColor, Color(0x66FFFFFF), accentColor.copy(alpha = 0.28f))
        )
    }

    val frameless = LocalFramelessAdminMode.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(brush = glassFill)
            .glassBorder(
                width = 1.2.dp,
                brush = topBorder,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                frameless = frameless
            )
            .then(
                if (frameless) {
                    Modifier
                } else {
                    Modifier.drawBehind {
                        drawLine(
                            brush = topHighlightLine,
                            start = Offset(size.width * 0.05f, 1.2f),
                            end = Offset(size.width * 0.95f, 1.2f),
                            strokeWidth = 1.6f
                        )
                    }
                }
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            val tabWidth = maxWidth / tabs.size
            val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }

            // Deferred-read GPU offset pill indicator (zero recompositions per frame)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((tabWidthPx * animatedTabOffsetState.value).roundToInt(), 0)
                    }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(activePillFill)
                    .glassBorder(
                        width = 1.2.dp,
                        brush = activePillBorder,
                        shape = RoundedCornerShape(18.dp),
                        frameless = frameless
                    )
            )

            // Tab items row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tabIndex, label, icon) ->
                    val isSelected = selectedTab == tabIndex

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = 0.72f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "tabScale_$tabIndex"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.88f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconTint_$tabIndex"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.82f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "textColor_$tabIndex"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onTabSelected(tabIndex) }
                            .testTag("tab_$tabIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.scale(scale)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = iconTint,
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingStateView(cityName: String) {
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x330F172A))
                .padding(32.dp)
        ) {
            CircularProgressIndicator(
                color = accentColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "${strings.loadingWeatherPrefix} $cityName…",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.loadingWeatherSubtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorStateView(
    error: String,
    onRetry: () -> Unit
) {
    val frameless = LocalFramelessAdminMode.current
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x440F172A))
                .glassBorder(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp), frameless)
                .padding(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0x22EF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = strings.errorWeatherTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = error,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("retry_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.retryButton,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
