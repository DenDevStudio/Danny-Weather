package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.AppAccentTheme
import com.example.data.model.AppCustomizationConfig
import com.example.data.model.City
import com.example.data.model.FullWeatherData
import com.example.data.model.KYIV_CITY
import com.example.data.model.NEW_YORK_CITY
import com.example.data.model.ReleaseUpdateNotice
import com.example.data.model.STRYI_CITY
import com.example.data.model.SkyType
import com.example.data.model.WeatherHomeBlock
import com.example.data.repository.CloudVipAccessEntry
import com.example.ui.viewmodel.WeatherViewModel
import java.security.MessageDigest

object SecretCredentials {
    private const val LOGIN_SALT = "dw_login_v2:"
    private const val PASS_SALT = "dw_pass_v2:"

    // Precomputed salted SHA-256 digests (zero plaintext secrets stored in source/APK)
    const val EXPECTED_LOGIN_SHA256 =
        "9ee1b146983feed3831d713e841cce2e7601f35e8f2a5db9fb253ecd9ee2b92c"
    const val EXPECTED_PASSWORD_SHA256 =
        "09ab8ff1d216bf77348769edfd579efa2d0a566942782d33a99535f6b76eea34"

    fun sha256Hex(rawInput: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(rawInput.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte ->
            ((byte.toInt() and 0xFF) + 0x100).toString(16).substring(1)
        }
    }

    fun isValid(login: String, password: String): Boolean {
        return isValidWithHashes(
            login = login,
            password = password,
            expectedLoginSha256 = EXPECTED_LOGIN_SHA256,
            expectedPasswordSha256 = EXPECTED_PASSWORD_SHA256
        )
    }

    fun isValidWithHashes(
        login: String,
        password: String,
        expectedLoginSha256: String,
        expectedPasswordSha256: String
    ): Boolean {
        val actualLoginBytes = sha256Hex(LOGIN_SALT + login.trim()).toByteArray(Charsets.UTF_8)
        val expectedLoginBytes = expectedLoginSha256.toByteArray(Charsets.UTF_8)
        val actualPassBytes = sha256Hex(PASS_SALT + password).toByteArray(Charsets.UTF_8)
        val expectedPassBytes = expectedPasswordSha256.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(actualLoginBytes, expectedLoginBytes) &&
            MessageDigest.isEqual(actualPassBytes, expectedPassBytes)
    }
}

class SecretGestureDetector {
    private var armedUntilMs: Long = 0L
    private var firstTapUpMs: Long = 0L

    fun onHoldCompleted(nowMs: Long) {
        armedUntilMs = nowMs + ARMED_WINDOW_MS
        firstTapUpMs = 0L
    }

    fun onTapReleased(pressDurationMs: Long, nowMs: Long): Boolean {
        if (nowMs > armedUntilMs) {
            reset()
            return false
        }
        if (pressDurationMs > MAX_TAP_DURATION_MS) {
            return false
        }
        return if (firstTapUpMs == 0L || (nowMs - firstTapUpMs) > DOUBLE_TAP_INTERVAL_MS) {
            firstTapUpMs = nowMs
            false
        } else {
            reset()
            true
        }
    }

    fun isArmed(nowMs: Long): Boolean = nowMs <= armedUntilMs

    fun reset() {
        armedUntilMs = 0L
        firstTapUpMs = 0L
    }

    companion object {
        const val HOLD_DURATION_MS = 5000L
        const val ARMED_WINDOW_MS = 10_000L
        const val DOUBLE_TAP_INTERVAL_MS = 2500L
        const val MAX_TAP_DURATION_MS = 1000L
    }
}

@Composable
fun SecretLoginDialog(
    errorMessage: String?,
    onSubmit: (login: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var loginText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xE6101B33),
                            Color(0xEB0B1222)
                        )
                    )
                )
                .glassBorder(1.2.dp, Color(0x55FFFFFF), RoundedCornerShape(28.dp), frameless = false)
                .padding(22.dp)
                .testTag("secret_login_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0x33FFD54F), CircleShape)
                                .glassBorder(1.dp, Color(0x66FFD54F), CircleShape, frameless = false),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Вхід",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Введіть облікові дані",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Закрити",
                            tint = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                OutlinedTextField(
                    value = loginText,
                    onValueChange = { loginText = it },
                    label = { Text("Логін") },
                    placeholder = { Text("Введіть логін", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD54F),
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedLabelColor = Color(0xFFFFD54F),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color(0xFFFFD54F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_login_input")
                )

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("Пароль") },
                    placeholder = { Text("Введіть пароль", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (passwordVisible) "Сховати пароль" else "Показати пароль",
                                tint = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit(loginText, passwordText) }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD54F),
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedLabelColor = Color(0xFFFFD54F),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color(0xFFFFD54F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_password_input")
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF8A80),
                        modifier = Modifier.testTag("secret_login_error")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Скасувати", color = Color.White.copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = { onSubmit(loginText, passwordText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("secret_login_submit_button")
                    ) {
                        Text(
                            text = "Увійти",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecretDeveloperMenuDialog(
    selectedCity: City,
    pinnedCitiesCount: Int,
    weatherData: FullWeatherData?,
    isAdminLegacyFrameless: Boolean,
    debugSkyOverride: SkyType?,
    homeBlockOrder: List<WeatherHomeBlock>,
    customization: AppCustomizationConfig,
    releaseUpdateNotice: ReleaseUpdateNotice,
    statusMessage: String?,
    isGoogleSignedIn: Boolean = false,
    cloudUserEmail: String? = null,
    isCloudSyncing: Boolean = false,
    vipEntries: List<CloudVipAccessEntry> = emptyList(),
    onAddVipEmail: (String) -> Unit = {},
    onRemoveVipEmail: (String) -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onToggleAdminLegacyFrameless: (Boolean) -> Unit,
    onUpdateAppTitle: (String) -> Unit,
    onUpdateHeaderBadge: (String) -> Unit,
    onUpdateVersionBannerText: (String) -> Unit,
    onMoveHomeBlock: (index: Int, delta: Int) -> Unit,
    onToggleHomeBlockVisibility: (WeatherHomeBlock) -> Unit,
    onResetHomeBlockOrder: () -> Unit,
    onToggleShowPinnedStrip: (Boolean) -> Unit,
    onToggleShowWorldCitiesTab: (Boolean) -> Unit,
    onSetAccentTheme: (AppAccentTheme) -> Unit,
    onSetCardCornerRadius: (Int) -> Unit,
    onSetGlassOpacity: (Float) -> Unit,
    onSetAnimateLiquidBackground: (Boolean) -> Unit,
    onSetDefaultPresetCity: (City) -> Unit,
    onResetAllCustomizations: () -> Unit,
    onPublishUpdateNotice: (version: String, notes: String) -> Unit,
    onClearUpdateNotice: () -> Unit,
    onRepairWidget: () -> Unit,
    onSelectSkyOverride: (SkyType?) -> Unit,
    onLogoutAdmin: () -> Unit,
    onDismiss: () -> Unit
) {
    val frameless = LocalFramelessAdminMode.current
    val accentColor = LocalAppAccentColor.current

    var selectedStudioTab by remember { mutableIntStateOf(0) }
    var appTitleDraft by remember(customization.appTitle) { mutableStateOf(customization.appTitle) }
    var badgeDraft by remember(customization.headerBadge) { mutableStateOf(customization.headerBadge) }
    var versionBannerDraft by remember(customization.versionBannerText) {
        mutableStateOf(customization.versionBannerText)
    }
    var versionInput by remember {
        mutableStateOf(
            releaseUpdateNotice.targetVersion.ifBlank {
                WeatherViewModel.suggestNextVersion(BuildConfig.VERSION_NAME)
            }
        )
    }
    var releaseNotesInput by remember {
        mutableStateOf(
            releaseUpdateNotice.releaseNotes.ifBlank {
                "Покращено стабільність, дизайн Рідке Скло та 14-денний прогноз."
            }
        )
    }
    var vipEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(releaseUpdateNotice.targetVersion, releaseUpdateNotice.releaseNotes) {
        if (releaseUpdateNotice.targetVersion.isNotBlank()) {
            versionInput = releaseUpdateNotice.targetVersion
        }
        if (releaseUpdateNotice.releaseNotes.isNotBlank()) {
            releaseNotesInput = releaseUpdateNotice.releaseNotes
        }
    }

    val skyPresets = remember {
        listOf(
            null to "Реальна погода",
            SkyType.CLEAR_DAY to "Ясний день",
            SkyType.CLEAR_NIGHT to "Зоряна ніч",
            SkyType.RAIN to "Злива",
            SkyType.THUNDERSTORM to "Гроза",
            SkyType.SNOW to "Снігопад",
            SkyType.FOG to "Туман"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 710.dp)
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF00F1930),
                            Color(0xF5090F1D)
                        )
                    )
                )
                .glassBorder(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(28.dp), frameless)
                .padding(18.dp)
                .testTag("secret_developer_menu_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeveloperMode,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DenDev Studio",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "Самостійний конструктор і випуск версій",
                                fontSize = 11.sp,
                                color = accentColor
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Закрити",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // 3-Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple(0, "Конструктор", Icons.Rounded.Tune),
                        Triple(1, "Дизайн", Icons.Rounded.Palette),
                        Triple(2, "Реліз та Доступ", Icons.Rounded.NewReleases)
                    ).forEach { (tabIdx, title, icon) ->
                        val active = selectedStudioTab == tabIdx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) accentColor else Color.Transparent)
                                .clickable { selectedStudioTab = tabIdx }
                                .padding(vertical = 8.dp)
                                .testTag("studio_tab_$tabIdx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) Color(0xFF0F172A) else Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) Color(0xFF0F172A) else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Scrollable body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedStudioTab) {
                        0 -> {
                            // TAB 0: CONSTRUCTOR (Texts, Default City, Blocks Order & Visibility, Toggles)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Назва та бейдж додатка",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = appTitleDraft,
                                        onValueChange = { appTitleDraft = it },
                                        label = { Text("Назва в шапці") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = studioTextFieldColors(accentColor),
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .testTag("studio_app_title_input")
                                    )
                                    OutlinedTextField(
                                        value = badgeDraft,
                                        onValueChange = { badgeDraft = it },
                                        label = { Text("Бейдж") },
                                        placeholder = { Text("v2.0") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = studioTextFieldColors(accentColor),
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .testTag("studio_badge_input")
                                    )
                                }
                                OutlinedTextField(
                                    value = versionBannerDraft,
                                    onValueChange = { versionBannerDraft = it },
                                    label = { Text("Рядок версії у Налаштуваннях") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = studioTextFieldColors(accentColor),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        onUpdateAppTitle(appTitleDraft)
                                        onUpdateHeaderBadge(badgeDraft)
                                        onUpdateVersionBannerText(versionBannerDraft)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("studio_apply_texts_button")
                                ) {
                                    Text(
                                        text = "Застосувати тексти та назву",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Default Startup City selector
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Стартове місто (${selectedCity.nameUk})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(NEW_YORK_CITY, KYIV_CITY, STRYI_CITY).forEach { preset ->
                                        val isCurrent = selectedCity.id == preset.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isCurrent) accentColor else Color(0x22FFFFFF))
                                                .clickable { onSetDefaultPresetCity(preset) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${preset.flagEmoji} ${preset.nameUk}",
                                                fontSize = 12.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrent) Color(0xFF0F172A) else Color.White
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x2238BDF8))
                                            .clickable { onSetDefaultPresetCity(selectedCity) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Зробити поточне стартовим",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF67E8F9)
                                        )
                                    }
                                }
                            }

                            // Home Blocks Reorder + Show/Hide Toggle
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x1C38BDF8))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.SwapVert,
                                            contentDescription = null,
                                            tint = Color(0xFF67E8F9),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Блоки: порядок і видимість",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    TextButton(onClick = onResetHomeBlockOrder) {
                                        Icon(
                                            imageVector = Icons.Rounded.RestartAlt,
                                            contentDescription = null,
                                            tint = Color(0xFF67E8F9),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Скинути", fontSize = 11.sp, color = Color(0xFF67E8F9))
                                    }
                                }

                                homeBlockOrder.forEachIndexed { index, block ->
                                    val isVisible = block !in customization.hiddenHomeBlocks
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x220F172A))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${block.titleUk}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isVisible) Color.White else Color.White.copy(alpha = 0.4f),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { onToggleHomeBlockVisibility(block) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("toggle_block_visibility_${block.name}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                                    contentDescription = if (isVisible) "Приховати блок" else "Показати блок",
                                                    tint = if (isVisible) Color(0xFF4ADE80) else Color(0xFFFF8A80),
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onMoveHomeBlock(index, -1) },
                                                enabled = index > 0,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("move_block_up_$index")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.KeyboardArrowUp,
                                                    contentDescription = "Вгору",
                                                    tint = if (index > 0) accentColor else Color.White.copy(alpha = 0.25f)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onMoveHomeBlock(index, 1) },
                                                enabled = index < homeBlockOrder.lastIndex,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("move_block_down_$index")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                                    contentDescription = "Вниз",
                                                    tint = if (index < homeBlockOrder.lastIndex) accentColor else Color.White.copy(alpha = 0.25f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Interface Switches
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StudioSwitchRow(
                                    title = "Панель міст зверху",
                                    subtitle = "Швидкі кнопки закріплених міст",
                                    checked = customization.showPinnedStrip,
                                    accentColor = accentColor,
                                    onCheckedChange = onToggleShowPinnedStrip
                                )
                                HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                                StudioSwitchRow(
                                    title = "Вкладка «Міста світу»",
                                    subtitle = "Довідник міст та країн у нижньому меню",
                                    checked = customization.showWorldCitiesTab,
                                    accentColor = accentColor,
                                    onCheckedChange = onToggleShowWorldCitiesTab
                                )
                                HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                                StudioSwitchRow(
                                    title = "Старша версія (тільки Адмін)",
                                    subtitle = "Стрий закріплено • без рамок",
                                    checked = isAdminLegacyFrameless,
                                    accentColor = accentColor,
                                    onCheckedChange = onToggleAdminLegacyFrameless,
                                    switchModifier = Modifier.testTag("admin_legacy_frameless_switch")
                                )
                            }
                        }

                        1 -> {
                            // TAB 1: DESIGN STUDIO (Colors, Corners, Glass Opacity, Sky, Widgets)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Акцентний колір інтерфейсу",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AppAccentTheme.entries.forEach { theme ->
                                        val isSelected = customization.accentTheme == theme
                                        val themeColor = Color(theme.colorHex)
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) themeColor.copy(alpha = 0.25f) else Color(0x18FFFFFF))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                                    color = if (isSelected) themeColor else Color(0x33FFFFFF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onSetAccentTheme(theme) }
                                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                                .testTag("accent_theme_${theme.name}"),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(themeColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0F172A),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = theme.labelUk,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Card Corner Radius & Glass Opacity
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Форма карток (заокруглення)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(16 to "Чіткі 16dp", 26 to "Стандарт 26dp", 32 to "М'які 32dp").forEach { (dpVal, label) ->
                                        val selected = customization.cardCornerRadiusDp == dpVal
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) accentColor else Color(0x20FFFFFF))
                                                .clickable { onSetCardCornerRadius(dpVal) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) Color(0xFF0F172A) else Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Прозорість ефекту «Рідке Скло»",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(0.6f to "Ультра-прозоре", 1.0f to "Стандарт", 1.4f to "Насичене").forEach { (level, label) ->
                                        val selected = kotlin.math.abs(customization.glassOpacityLevel - level) < 0.15f
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) accentColor else Color(0x20FFFFFF))
                                                .clickable { onSetGlassOpacity(level) }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) Color(0xFF0F172A) else Color.White
                                            )
                                        }
                                    }
                                }

                                StudioSwitchRow(
                                    title = "Жива анімація фонових крапель",
                                    subtitle = "Вимкніть для економії батареї",
                                    checked = customization.animateLiquidBackground,
                                    accentColor = accentColor,
                                    onCheckedChange = onSetAnimateLiquidBackground
                                )
                            }

                            // Saved Sky Theme
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Постійний фон неба (Sky Shader)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    skyPresets.forEach { (sky, label) ->
                                        val selected = debugSkyOverride == sky
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) accentColor else Color(0x1EFFFFFF))
                                                .clickable { onSelectSkyOverride(sky) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selected) Color(0xFF0F172A) else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Widget Repair + Reset Design
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRepairWidget,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("repair_widget_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Widgets,
                                        contentDescription = null,
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Оновити віджети", fontSize = 11.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = onResetAllCustomizations,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RestartAlt,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Скинути v2.0", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        else -> {
                            // TAB 2: РЕЛІЗ ТА ДОСТУП (Version, Notes, Broadcast Button, VIP Whitelist)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x1EFFFFFF))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Розсилка оновлення (Google Play)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )

                                OutlinedTextField(
                                    value = versionInput,
                                    onValueChange = { versionInput = it },
                                    label = { Text("Номер нової версії (напр. 2.1)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = studioTextFieldColors(accentColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_release_version_input")
                                )

                                OutlinedTextField(
                                    value = releaseNotesInput,
                                    onValueChange = { releaseNotesInput = it },
                                    label = { Text("Що нового у цьому оновленні") },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = studioTextFieldColors(accentColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_release_notes_input")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x18FFFFFF))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isGoogleSignedIn) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                            contentDescription = null,
                                            tint = if (isGoogleSignedIn) Color(0xFF4ADE80) else Color(0xFFFFD54F),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isGoogleSignedIn) {
                                                "Firebase: ${cloudUserEmail ?: "підключено"}"
                                            } else {
                                                "Увійдіть через Google для хмарного запису"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (!isGoogleSignedIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        TextButton(
                                            onClick = onSignInWithGoogle,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.testTag("studio_google_sign_in_button")
                                        ) {
                                            Text(
                                                text = "Увійти",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onPublishUpdateNotice(versionInput, releaseNotesInput) },
                                    enabled = !isCloudSyncing,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_publish_update_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NewReleases,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCloudSyncing) {
                                            "Надсилання у Firebase..."
                                        } else {
                                            "Сповістити всіх про оновлення"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (releaseUpdateNotice.isAvailable) {
                                    TextButton(
                                        onClick = onClearUpdateNotice,
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            text = "Приховати сповіщення",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.72f),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // VIP-доступ (Білий список email)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(12.dp)
                                    .testTag("vip_whitelist_section"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "VIP-доступ (Білий список email)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4ADE80)
                                )
                                Text(
                                    text = "Рідні та друзі з цього списку безкоштовно отримують PRO-прогноз на 14 днів",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.72f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = vipEmailInput,
                                        onValueChange = { vipEmailInput = it },
                                        label = { Text("Email друга або рідного") },
                                        placeholder = { Text("friend@gmail.com") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (vipEmailInput.isNotBlank()) {
                                                    onAddVipEmail(vipEmailInput)
                                                    vipEmailInput = ""
                                                }
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = studioTextFieldColors(accentColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("vip_email_input")
                                    )

                                    Button(
                                        onClick = {
                                            if (vipEmailInput.isNotBlank()) {
                                                onAddVipEmail(vipEmailInput)
                                                vipEmailInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accentColor,
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                        modifier = Modifier.testTag("vip_email_add_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Додати VIP email",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (vipEntries.isEmpty()) {
                                    Text(
                                        text = "Список VIP-користувачів поки порожній",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                } else {
                                    vipEntries.forEach { entry ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x220F172A))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = entry.email,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { onRemoveVipEmail(entry.id) },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("vip_email_remove_${entry.email}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "Видалити ${entry.email}",
                                                    tint = Color(0xFFFF8A80),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!statusMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x224ADE80))
                            .glassBorder(0.8.dp, Color(0x554ADE80), RoundedCornerShape(12.dp), frameless)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = statusMessage,
                            fontSize = 11.sp,
                            color = Color(0xFFBBF7D0),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLogoutAdmin,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Вийти",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text(
                            text = "Готово",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    switchModifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.68f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF0F172A),
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0x33FFFFFF)
            ),
            modifier = switchModifier
        )
    }
}

@Composable
private fun studioTextFieldColors(accentColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = accentColor,
    unfocusedBorderColor = Color(0x44FFFFFF),
    focusedLabelColor = accentColor,
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
    cursorColor = accentColor
)

@Composable
private fun SecretInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
