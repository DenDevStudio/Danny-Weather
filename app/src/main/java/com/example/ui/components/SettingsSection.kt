package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCustomizationConfig
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserWeatherSettings
import com.example.data.model.WindUnit
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import kotlinx.coroutines.delay

@Composable
fun SettingsSection(
    settings: UserWeatherSettings,
    onTempUnitChange: (TemperatureUnit) -> Unit,
    onPressureUnitChange: (PressureUnit) -> Unit,
    onWindUnitChange: (WindUnit) -> Unit,
    appLanguageOverride: AppLanguage? = null,
    onAppLanguageChange: (AppLanguage?) -> Unit = {},
    onOpenSecretLogin: () -> Unit = {},
    versionBannerText: String = AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK,
    isGoogleSignedIn: Boolean = false,
    cloudUserEmail: String? = null,
    isProUnlocked: Boolean = false,
    isVipWhitelisted: Boolean = false,
    isAdminMode: Boolean = false,
    onOpenProUpgrade: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    val frameless = LocalFramelessAdminMode.current
    val accentColor = LocalAppAccentColor.current
    val secretGestureDetector = remember { SecretGestureDetector() }
    var stepOneVisible by remember { mutableStateOf(false) }
    var stepOneTimerRunning by remember { mutableStateOf(false) }
    var stepOneDeadlineMs by remember { mutableLongStateOf(0L) }
    var remainingSeconds by remember {
        mutableIntStateOf((SecretGestureDetector.ARMED_WINDOW_MS / 1000L).toInt())
    }

    LaunchedEffect(stepOneTimerRunning, stepOneDeadlineMs) {
        if (!stepOneTimerRunning) return@LaunchedEffect
        while (true) {
            val remainingMs = stepOneDeadlineMs - System.currentTimeMillis()
            if (remainingMs <= 0L) {
                stepOneVisible = false
                stepOneTimerRunning = false
                secretGestureDetector.reset()
                break
            }
            remainingSeconds = ((remainingMs + 999L) / 1000L).toInt().coerceAtLeast(1)
            delay(100L)
        }
    }
    LiquidGlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("settings_section"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.settingsTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // App Language Selector (System Auto + Detected languages)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.settingsLanguageTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isAuto = appLanguageOverride == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAuto) accentColor else Color(0x1AFFFFFF))
                            .clickable { onAppLanguageChange(null) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("lang_chip_auto"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌐 ${strings.settingsLanguageSystem}",
                            fontSize = 12.sp,
                            fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAuto) Color(0xFF0F172A) else Color.White.copy(alpha = 0.85f)
                        )
                    }

                    AppLanguage.MANUAL_SELECTION_ORDER.forEach { lang ->
                        val isSelected = appLanguageOverride == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accentColor else Color(0x1AFFFFFF))
                                .clickable { onAppLanguageChange(lang) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("lang_chip_${lang.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${lang.flagEmoji} ${lang.nativeName}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Temperature Unit
            UnitToggleRow(
                title = strings.settingsTempTitle,
                icon = Icons.Rounded.Thermostat,
                options = listOf(
                    TemperatureUnit.CELSIUS to strings.settingsTempCelsius,
                    TemperatureUnit.FAHRENHEIT to strings.settingsTempFahrenheit
                ),
                selectedOption = settings.tempUnit,
                onSelect = onTempUnitChange
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Pressure Unit
            UnitToggleRow(
                title = strings.settingsPressureTitle,
                icon = Icons.Rounded.Compress,
                options = listOf(
                    PressureUnit.MMHG to strings.settingsPressureMmhg,
                    PressureUnit.HPA to strings.settingsPressureHpa
                ),
                selectedOption = settings.pressureUnit,
                onSelect = onPressureUnitChange
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Wind Unit
            UnitToggleRow(
                title = strings.settingsWindTitle,
                icon = Icons.Rounded.Air,
                options = listOf(
                    WindUnit.MS to strings.settingsWindMs,
                    WindUnit.KMH to strings.settingsWindKmh
                ),
                selectedOption = settings.windUnit,
                onSelect = onWindUnitChange
            )

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Calm Liquid Glass Danny Weather PRO Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isProUnlocked) Color(0x184ADE80) else Color(0x14FFFFFF))
                    .glassBorder(
                        0.8.dp,
                        if (isProUnlocked) Color(0x554ADE80) else accentColor.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp),
                        frameless
                    )
                    .padding(14.dp)
                    .testTag("settings_pro_card"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isProUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = if (isProUnlocked) Color(0xFF4ADE80) else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.proCardTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = when {
                                isVipWhitelisted || isAdminMode -> strings.proCardActiveVipSubtitle
                                isProUnlocked -> strings.proCardActiveProSubtitle
                                else -> strings.proCardLockedSubtitle
                            },
                            fontSize = 11.5.sp,
                            color = if (isProUnlocked) Color(0xFFBBF7D0) else Color.White.copy(alpha = 0.74f)
                        )
                    }
                }

                if (!isProUnlocked) {
                    Button(
                        onClick = onOpenProUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_pro_upgrade_button")
                    ) {
                        Text(
                            text = strings.proUnlockButtonLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Cloud Account Sign-In Card (simple: sign in, or show connected account + sign out)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x14FFFFFF))
                    .glassBorder(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp), frameless)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isGoogleSignedIn) Icons.Rounded.CloudDone else Icons.Rounded.CloudQueue,
                            contentDescription = null,
                            tint = if (isGoogleSignedIn) Color(0xFF4ADE80) else accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = strings.cloudUpdatesTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isGoogleSignedIn) {
                                    "${strings.cloudConnectedPrefix} ${cloudUserEmail ?: "Google"}"
                                } else {
                                    strings.cloudSignInSubtitle
                                },
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.74f)
                            )
                        }
                    }

                    if (isGoogleSignedIn) {
                        TextButton(
                            onClick = onSignOutGoogle,
                            modifier = Modifier.testTag("settings_google_sign_out_button")
                        ) {
                            Text(strings.signOutLabel, fontSize = 12.sp, color = Color(0xFFFF8A80))
                        }
                    }
                }

                if (!isGoogleSignedIn) {
                    Button(
                        onClick = onSignInWithGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_google_sign_in_button")
                    ) {
                        Text(
                            text = strings.signInWithGoogleLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // About Danny Weather (entire version block accepts 5s hold + relaxed double-tap)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x18FFFFFF))
                    .glassBorder(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp), frameless)
                    .padding(14.dp)
                    .testTag("version_info_row")
                    .pointerInput(Unit) {
                        val holdSlopPx = 80.dp.toPx()
                        val tapSlopPx = 48.dp.toPx()
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            val pressStartMs = System.currentTimeMillis()
                            var maxDistancePx = 0f
                            val earlyOutcome = withTimeoutOrNull(SecretGestureDetector.HOLD_DURATION_MS) {
                                var outcome = 0
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val pointer = event.changes.firstOrNull { it.id == down.id }
                                    if (pointer == null || !pointer.pressed) {
                                        outcome = if (maxDistancePx <= tapSlopPx) 1 else 0
                                        break
                                    }
                                    val distance = (pointer.position - down.position).getDistance()
                                    if (distance > maxDistancePx) maxDistancePx = distance
                                    if (distance > holdSlopPx) {
                                        outcome = 0
                                        break
                                    }
                                }
                                outcome
                            }

                            if (earlyOutcome == null) {
                                // 5 seconds continuous hold completed anywhere on the version block
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                stepOneVisible = true
                                stepOneTimerRunning = false
                                remainingSeconds = (SecretGestureDetector.ARMED_WINDOW_MS / 1000L).toInt()
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val pointer = event.changes.firstOrNull { it.id == down.id }
                                    if (pointer == null || !pointer.pressed) break
                                }
                                val completedAt = System.currentTimeMillis()
                                secretGestureDetector.onHoldCompleted(completedAt)
                                stepOneDeadlineMs = completedAt + SecretGestureDetector.ARMED_WINDOW_MS
                                stepOneTimerRunning = true
                            } else if (earlyOutcome == 1) {
                                val nowMs = System.currentTimeMillis()
                                val pressDurationMs = nowMs - pressStartMs
                                if (secretGestureDetector.onTapReleased(pressDurationMs, nowMs)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    stepOneVisible = false
                                    stepOneTimerRunning = false
                                    onOpenSecretLogin()
                                }
                            }
                        }
                    },
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val resolvedVersionBanner = if (
                        versionBannerText.isBlank() ||
                        versionBannerText == AppCustomizationConfig.DEFAULT_VERSION_BANNER_UK
                    ) {
                        strings.defaultVersionBanner
                    } else {
                        versionBannerText
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = resolvedVersionBanner,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    AnimatedVisibility(
                        visible = stepOneVisible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 2.dp)
                                .testTag("secret_step_one_timer"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.firstStepCompleted,
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.05f)
                            )
                            Text(
                                text = if (stepOneTimerRunning) "${remainingSeconds}s" else "•",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }
                    Text(
                        text = strings.copyrightFooter,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .testTag("dendev_copyright_footer")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.aboutDescriptionText,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> UnitToggleRow(
    title: String,
    icon: ImageVector,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val frameless = LocalFramelessAdminMode.current
        val accentColor = LocalAppAccentColor.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x14FFFFFF))
                .glassBorder(0.8.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp), frameless)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (option, label) ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) accentColor else Color.Transparent)
                        .clickable { onSelect(option) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
