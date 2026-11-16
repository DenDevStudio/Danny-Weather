package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.City
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.WeatherCodeMapper
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.localizedCountry
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedSubtitle

@Composable
fun CurrentWeatherCard(
    city: City,
    current: CurrentWeather,
    todayForecast: DailyForecastItem?,
    formattedTemp: String,
    formattedFeelsLike: String,
    isRefreshing: Boolean,
    onCityClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    val lang = strings.language
    LiquidGlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City header with switch affordance
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onCityClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("city_selector_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = strings.locationCd,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = city.localizedName(lang),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (city.isPrimary) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.22f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = strings.primaryCityBadge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = strings.changeCityCd,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = city.localizedSubtitle(lang),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Refresh button
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = accentColor,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = strings.refreshWeatherCd,
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main temperature and condition badge
            WeatherIcon(
                condition = current.condition,
                size = 72.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedTemp,
                fontSize = 68.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = (-2).sp
            )

            Text(
                text = WeatherCodeMapper.localizedConditionName(
                    code = current.condition.code,
                    isDay = current.isDay,
                    language = lang
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${strings.feelsLikePrefix} $formattedFeelsLike",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            // High / Low temperatures for today
            if (todayForecast != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxRound = Math.round(todayForecast.maxTempCelsius).toInt()
                    val minRound = Math.round(todayForecast.minTempCelsius).toInt()
                    Text(
                        text = "${strings.maxLabel}: ${if (maxRound > 0) "+$maxRound" else "$maxRound"}°",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    Text(
                        text = "  •  ",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${strings.minLabel}: ${if (minRound > 0) "+$minRound" else "$minRound"}°",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF90CAF9)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live status & City/Country badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1A000000))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4ADE80), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val updatedLabel = if (current.lastUpdatedTime.isBlank() || current.lastUpdatedTime == "Зараз") {
                        strings.nowLabel
                    } else {
                        current.lastUpdatedTime
                    }
                    Text(
                        text = "${strings.updatedPrefix} $updatedLabel",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Text(
                    text = "${city.flagEmoji} ${city.localizedCountry(lang)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor.copy(alpha = 0.95f)
                )
            }
        }
    }
}
