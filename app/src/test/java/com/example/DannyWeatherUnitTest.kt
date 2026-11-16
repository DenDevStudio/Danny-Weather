package com.example

import com.example.data.model.DEFAULT_CITY
import com.example.data.model.KYIV_CITY
import com.example.data.model.NEW_YORK_CITY
import com.example.data.model.STRYI_CITY
import com.example.data.model.SkyType
import com.example.data.model.WORLD_CITIES
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherHomeBlock
import com.example.data.model.WorldRegion
import com.example.data.model.countryCodeToFlag
import com.example.ui.components.SecretCredentials
import com.example.ui.components.SecretGestureDetector
import com.example.ui.viewmodel.WeatherViewModel
import com.example.util.UpdateNotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DannyWeatherUnitTest {

    @Test
    fun playStorePackageId_containsNoAiStudioBranding() {
        assertFalse(BuildConfig.APPLICATION_ID.contains("aistudio", ignoreCase = true))
        assertEquals("com.dendevstudio.dannyweather", BuildConfig.APPLICATION_ID)
    }

    @Test
    fun homeBlockSwap_reordersCardsSafely() {
        val initial = WeatherHomeBlock.DEFAULT_ORDER
        val movedDown = WeatherViewModel.swapHomeBlocks(initial, 0, 1)
        assertEquals(
            listOf(
                WeatherHomeBlock.HOURLY,
                WeatherHomeBlock.CURRENT,
                WeatherHomeBlock.METRICS,
                WeatherHomeBlock.AIR_QUALITY
            ),
            movedDown
        )

        // Out-of-bounds swap leaves list unchanged
        assertEquals(initial, WeatherViewModel.swapHomeBlocks(initial, 0, -1))
        assertEquals(initial, WeatherViewModel.swapHomeBlocks(initial, 3, 1))
        assertEquals("2.1", WeatherViewModel.suggestNextVersion("2.0 (V2)"))
        assertEquals("2.2", WeatherViewModel.suggestNextVersion("2.1"))
        assertEquals("2.0", BuildConfig.VERSION_NAME)
        assertEquals("v2.0", com.example.data.model.AppCustomizationConfig().headerBadge)
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.dendevstudio.dannyweather",
            UpdateNotificationHelper.playMarketWebUrl()
        )
    }

    @Test
    fun cloudVersionComparison_detectsOnlyStrictlyNewerSemverVersions() {
        assertTrue(WeatherViewModel.isCloudVersionNewer("2.1", "2.0 (V2)"))
        assertTrue(WeatherViewModel.isCloudVersionNewer("2.0.1", "2.0 (V2)"))
        assertTrue(WeatherViewModel.isCloudVersionNewer("3.0", "2.9"))
        assertFalse(WeatherViewModel.isCloudVersionNewer("2.0", "2.0 (V2)"))
        assertFalse(WeatherViewModel.isCloudVersionNewer("1.9", "2.0 (V2)"))
        assertFalse(WeatherViewModel.isCloudVersionNewer("", "2.0 (V2)"))
    }

    @Test
    fun proAccess_grantsAccessForAdminVipAndPaidProOnly() {
        val freeUser = com.example.ui.viewmodel.WeatherUiState()
        assertFalse(freeUser.isProAccessGranted)

        assertTrue(freeUser.copy(isAdminMode = true).isProAccessGranted)
        assertTrue(freeUser.copy(isVipWhitelisted = true).isProAccessGranted)
        assertTrue(freeUser.copy(isProSubscribed = true).isProAccessGranted)
    }

    @Test
    fun localization_autoDetectsLanguageAndIncludesCopyrightAndDisclaimer() {
        assertEquals(
            com.example.ui.i18n.AppLanguage.UKRAINIAN,
            com.example.ui.i18n.AppLanguage.fromLocale(java.util.Locale.forLanguageTag("uk-UA"))
        )
        assertEquals(
            com.example.ui.i18n.AppLanguage.POLISH,
            com.example.ui.i18n.AppLanguage.fromLocale(java.util.Locale.forLanguageTag("pl-PL"))
        )
        assertEquals(
            com.example.ui.i18n.AppLanguage.ENGLISH,
            com.example.ui.i18n.AppLanguage.fromLocale(java.util.Locale.forLanguageTag("ja-JP"))
        )
        assertEquals(
            com.example.ui.i18n.AppLanguage.UKRAINIAN,
            com.example.ui.i18n.AppLanguage.fromLocaleList(
                androidx.core.os.LocaleListCompat.forLanguageTags("ja-JP,uk-UA,en-US")
            )
        )

        val ukStrings = com.example.ui.i18n.AppStrings.forLanguage(com.example.ui.i18n.AppLanguage.UKRAINIAN)
        assertEquals("14 Днів", ukStrings.tabSevenDays)
        assertEquals("© 2026 DenDev Studio. All rights reserved.", ukStrings.copyrightFooter)
        assertTrue(ukStrings.honestForecastDisclaimer.contains("4–5 днів"))
        assertFalse(ukStrings.updateNowButton.contains("Play", ignoreCase = true))

        val manualOrder = com.example.ui.i18n.AppLanguage.MANUAL_SELECTION_ORDER
        assertEquals(com.example.ui.i18n.AppLanguage.ENGLISH, manualOrder.first())
        val remainingNames = manualOrder.drop(1).map { it.nativeName }
        assertEquals(
            listOf("Deutsch", "Español", "Français", "Polski", "Українська"),
            remainingNames
        )

        val enStrings = com.example.ui.i18n.AppStrings.forLanguage(com.example.ui.i18n.AppLanguage.ENGLISH)
        val cyrillicRegex = Regex("[А-Яа-яЇїІіЄєҐґ]")
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.defaultVersionBanner))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.playStoreOpenError))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.googleConnectedStatus))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.googleSignInSuccessToast))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.googleSignedOutStatus))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.cloudSyncErrorFallback))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.weatherLoadErrorFallback))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.accountUpdateTitle))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.accountUpdateSubtitle))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.accountUpdateBody))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.openPlayMarketButton))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.cloudAccountConnectedTitle))
        assertFalse(cyrillicRegex.containsMatchIn(enStrings.cloudAccountSwitchButton))
        assertTrue(enStrings.accountUpdateBody.contains("Google Play Market"))
        assertTrue(ukStrings.accountUpdateBody.contains("Google Play Market"))
    }

    @Test
    fun defaultCity_isUnitedStatesAndNotUkraine() {
        assertFalse(STRYI_CITY.isPrimary)
        assertFalse(DEFAULT_CITY.id == STRYI_CITY.id)
        assertFalse(DEFAULT_CITY.id == KYIV_CITY.id)
        assertEquals(NEW_YORK_CITY.id, DEFAULT_CITY.id)
        assertEquals("US", DEFAULT_CITY.countryCode)
        assertEquals(WorldRegion.AMERICAS, DEFAULT_CITY.worldRegion)
        assertEquals(NEW_YORK_CITY.id, WORLD_CITIES.first().id)
        assertEquals(WorldRegion.AMERICAS, WorldRegion.entries[1])
        assertEquals(WorldRegion.UKRAINE, WorldRegion.entries.last())
    }

    @Test
    fun worldCitiesCatalog_containsAllRegionsAndCountries() {
        val regions = WORLD_CITIES.map { it.worldRegion }.toSet()
        assertTrue(regions.contains(WorldRegion.UKRAINE))
        assertTrue(regions.contains(WorldRegion.EUROPE))
        assertTrue(regions.contains(WorldRegion.ASIA))
        assertTrue(regions.contains(WorldRegion.AMERICAS))
        assertTrue(regions.contains(WorldRegion.AFRICA))
        assertTrue(regions.contains(WorldRegion.OCEANIA))

        val countries = WORLD_CITIES.map { it.countryCode }.toSet()
        assertTrue(countries.size >= 30)
        assertFalse(WORLD_CITIES.any { it.id == "berezhany_ternopil" || it.nameUk.equals("Бережани", ignoreCase = true) })
    }

    @Test
    fun countryCodeToFlag_generatesValidFlags() {
        assertEquals("🇺🇦", countryCodeToFlag("UA"))
        assertEquals("🇬🇧", countryCodeToFlag("gb"))
        assertEquals("🇯🇵", countryCodeToFlag("JP"))
        assertEquals("🌍", countryCodeToFlag(null))
    }

    @Test
    fun weatherCodeMapper_returnsCorrectConditions() {
        val clearDay = WeatherCodeMapper.map(0, isDay = true)
        assertEquals("Ясно", clearDay.nameUk)
        assertEquals(SkyType.CLEAR_DAY, clearDay.skyType)

        val clearNight = WeatherCodeMapper.map(0, isDay = false)
        assertEquals("Ясна ніч", clearNight.nameUk)
        assertEquals(SkyType.CLEAR_NIGHT, clearNight.skyType)

        val rain = WeatherCodeMapper.map(63, isDay = true)
        assertEquals("Помірний дощ", rain.nameUk)
        assertEquals(SkyType.RAIN, rain.skyType)

        val thunderstorm = WeatherCodeMapper.map(95, isDay = true)
        assertEquals("Гроза", thunderstorm.nameUk)
        assertEquals(SkyType.THUNDERSTORM, thunderstorm.skyType)

        val snow = WeatherCodeMapper.map(71, isDay = true)
        assertEquals("Невеликий снігопад", snow.nameUk)
        assertEquals(SkyType.SNOW, snow.skyType)
    }

    @Test
    fun weatherCodeMapper_returnsWindDirectionInUkrainian() {
        assertEquals("Північний (Пн)", WeatherCodeMapper.getWindDirectionUk(0))
        assertEquals("Східний (Сх)", WeatherCodeMapper.getWindDirectionUk(90))
        assertEquals("Південний (Пд)", WeatherCodeMapper.getWindDirectionUk(180))
        assertEquals("Західний (Зх)", WeatherCodeMapper.getWindDirectionUk(270))
        assertEquals("Північно-західний (Пн-Зх)", WeatherCodeMapper.getWindDirectionUk(315))
    }

    @Test
    fun weatherCodeMapper_returnsAqiLevelsAndAdvice() {
        val (levelLow, adviceLow) = WeatherCodeMapper.getAqiLevelUk(15)
        assertEquals("Відмінна", levelLow)
        assertFalse(adviceLow.contains("Стрий"))

        val (levelMod, _) = WeatherCodeMapper.getAqiLevelUk(50)
        assertEquals("Помірна", levelMod)

        val (levelPoor, _) = WeatherCodeMapper.getAqiLevelUk(90)
        assertEquals("Погана", levelPoor)
    }

    @Test
    fun unitConversions_areMathematicallyAccurate() {
        val tempC = 20.0
        val tempF = (tempC * 9.0 / 5.0) + 32.0
        assertEquals(68.0, tempF, 0.1)

        val pressureHpa = 1013.25
        val pressureMmHg = pressureHpa * 0.750062
        assertEquals(760.0, pressureMmHg, 0.5)

        val windMs = 10.0
        val windKmh = windMs * 3.6
        assertEquals(36.0, windKmh, 0.01)
    }

    @Test
    fun secretCredentials_validatesSha256DigestsSafelyWithoutPlaintext() {
        val sampleLogin = "TestAdmin"
        val samplePass = "TestPass123"
        val expectedLoginHash = SecretCredentials.sha256Hex("dw_login_v2:$sampleLogin")
        val expectedPassHash = SecretCredentials.sha256Hex("dw_pass_v2:$samplePass")

        assertTrue(
            SecretCredentials.isValidWithHashes(
                sampleLogin,
                samplePass,
                expectedLoginHash,
                expectedPassHash
            )
        )
        assertTrue(
            SecretCredentials.isValidWithHashes(
                "  $sampleLogin  ",
                samplePass,
                expectedLoginHash,
                expectedPassHash
            )
        )
        assertFalse(
            SecretCredentials.isValidWithHashes(
                sampleLogin.lowercase(),
                samplePass,
                expectedLoginHash,
                expectedPassHash
            )
        )
        assertFalse(
            SecretCredentials.isValidWithHashes(
                sampleLogin,
                "wrong",
                expectedLoginHash,
                expectedPassHash
            )
        )
        assertFalse(SecretCredentials.isValid("", ""))
        assertEquals(64, SecretCredentials.EXPECTED_LOGIN_SHA256.length)
        assertEquals(64, SecretCredentials.EXPECTED_PASSWORD_SHA256.length)
    }

    @Test
    fun secretGestureDetector_requires5sHoldFollowedByRelaxedDoubleTap() {
        val detector = SecretGestureDetector()

        // Double tap without prior 5s hold must NOT unlock
        assertFalse(detector.onTapReleased(pressDurationMs = 80L, nowMs = 1000L))
        assertFalse(detector.onTapReleased(pressDurationMs = 80L, nowMs = 1500L))

        // After 5s hold completes, calm "раз — два" double tap (1.5s apart) unlocks
        detector.onHoldCompleted(nowMs = 10_000L)
        assertTrue(detector.isArmed(10_500L))
        assertFalse(detector.onTapReleased(pressDurationMs = 120L, nowMs = 10_500L))
        assertTrue(detector.onTapReleased(pressDurationMs = 120L, nowMs = 12_000L))

        // Detector resets after successful unlock
        assertFalse(detector.isArmed(12_100L))
        assertFalse(detector.onTapReleased(pressDurationMs = 90L, nowMs = 12_200L))
    }

    @Test
    fun secretGestureDetector_expiresIfDoubleTapIsTooLate() {
        val detector = SecretGestureDetector()
        detector.onHoldCompleted(nowMs = 20_000L)
        // Window expired (> 10s after lifting)
        assertFalse(detector.isArmed(30_500L))
        assertFalse(detector.onTapReleased(pressDurationMs = 80L, nowMs = 30_500L))
        assertFalse(detector.onTapReleased(pressDurationMs = 80L, nowMs = 30_900L))
    }
}
