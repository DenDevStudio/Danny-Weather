package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItem
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.formatLocalizedDateLabel
import com.example.ui.i18n.formatLocalizedDayOfWeek

@Composable
fun DailyForecastSection(
    cityName: String,
    dailyItems: List<DailyForecastItem>,
    formatTemp: (Double) -> String,
    isProUnlocked: Boolean = false,
    onOpenProUpgrade: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (dailyItems.isEmpty()) return
    val strings = LocalAppStrings.current
    val accentColor = LocalAppAccentColor.current
    val frameless = LocalFramelessAdminMode.current

    val visibleItems = if (isProUnlocked) dailyItems else dailyItems.take(7)
    val lockedExtraCount = (dailyItems.size - 7).coerceAtLeast(0)

    val minOfAll = visibleItems.minOfOrNull { it.minTempCelsius } ?: 0.0
    val maxOfAll = visibleItems.maxOfOrNull { it.maxTempCelsius } ?: 20.0
    val span = (maxOfAll - minOfAll).coerceAtLeast(1.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LiquidGlassCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("daily_forecast_section")
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
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.sevenDayForecastPrefix} • $cityName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    if (isProUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .glassBorder(0.8.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp), frameless)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                visibleItems.forEachIndexed { index, item ->
                    DailyForecastRowItem(
                        item = item,
                        minOfAll = minOfAll,
                        maxOfAll = maxOfAll,
                        span = span,
                        formatTemp = formatTemp
                    )
                    if (index < visibleItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 0.5.dp,
                            color = Color(0x1AFFFFFF)
                        )
                    }
                }
            }
        }

        if (!isProUnlocked && lockedExtraCount > 0) {
            LiquidGlassCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("daily_pro_lock_banner"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.proLockBannerTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = strings.proLockBannerSubtitle,
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenProUpgrade,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_unlock_pro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.proUnlockButtonLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Honest long-range 14-day accuracy disclaimer card
        LiquidGlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .testTag("daily_forecast_disclaimer"),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(17.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.honestForecastDisclaimer,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRowItem(
    item: DailyForecastItem,
    minOfAll: Double,
    maxOfAll: Double,
    span: Double,
    formatTemp: (Double) -> String
) {
    val strings = LocalAppStrings.current
    val isToday = item.isToday || item.dayOfWeekUk == "Сьогодні"
    val dayTitle = formatLocalizedDayOfWeek(
        isoDate = item.isoDate,
        fallbackDayUk = item.dayOfWeekUk,
        isToday = isToday,
        strings = strings
    )
    val dateText = formatLocalizedDateLabel(
        isoDate = item.isoDate,
        fallbackDateLabel = item.dateLabel,
        language = strings.language
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val accentColor = LocalAppAccentColor.current
        // Day name & Date
        Column(modifier = Modifier.width(96.dp)) {
            Text(
                text = dayTitle,
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) accentColor else Color.White
            )
            Text(
                text = dateText,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Weather icon & precip
        Row(
            modifier = Modifier.width(55.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIcon(
                condition = item.condition,
                size = 24.dp
            )
            if (item.precipitationProbMax > 20) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.precipitationProbMax}%",
                    fontSize = 11.sp,
                    color = Color(0xFF67E8F9),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Min temp
        Text(
            text = formatTemp(item.minTempCelsius),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF90CAF9),
            modifier = Modifier.width(36.dp)
        )

        // Visual gradient temperature range bar
        val startFraction = ((item.minTempCelsius - minOfAll) / span).toFloat().coerceIn(0f, 1f)
        val endFraction = ((item.maxTempCelsius - minOfAll) / span).toFloat().coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (startFraction > 0f) {
                    Spacer(modifier = Modifier.weight(startFraction.coerceAtLeast(0.01f)))
                }
                Box(
                    modifier = Modifier
                        .weight((endFraction - startFraction).coerceAtLeast(0.1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), accentColor)
                            )
                        )
                )
                if (1f - endFraction > 0f) {
                    Spacer(modifier = Modifier.weight((1f - endFraction).coerceAtLeast(0.01f)))
                }
            }
        }

        // Max temp
        Text(
            text = formatTemp(item.maxTempCelsius),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.width(38.dp)
        )
    }
}
