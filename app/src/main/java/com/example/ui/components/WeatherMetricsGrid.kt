package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.WeatherCodeMapper
import com.example.ui.i18n.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherMetricsGrid(
    current: CurrentWeather,
    todayForecast: DailyForecastItem?,
    formatWind: (Double) -> String,
    formatPressure: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Wind & Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = strings.metricWindTitle,
                icon = Icons.Rounded.Air,
                value = formatWind(current.windSpeedKmh),
                subtitle = WeatherCodeMapper.getWindDirection(current.windDirectionDeg, strings.language),
                modifier = Modifier.weight(1f),
                badge = {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = strings.windDirectionCd,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(current.windDirectionDeg.toFloat())
                    )
                }
            )

            MetricCard(
                title = strings.metricHumidityTitle,
                icon = Icons.Rounded.Opacity,
                value = "${current.humidityPercent}%",
                subtitle = when {
                    current.humidityPercent > 80 -> strings.humidityHigh
                    current.humidityPercent < 40 -> strings.humidityDry
                    else -> strings.humidityComfort
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Atmospheric Pressure & UV Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = strings.metricPressureTitle,
                icon = Icons.Rounded.Compress,
                value = formatPressure(current.pressureHpa),
                subtitle = when {
                    current.pressureHpa > 1020 -> strings.pressureHigh
                    current.pressureHpa < 1005 -> strings.pressureLow
                    else -> strings.pressureNormal
                },
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = strings.metricUvTitle,
                icon = Icons.Rounded.WbSunny,
                value = String.format("%.1f", current.uvIndex),
                subtitle = when {
                    current.uvIndex < 3 -> strings.uvLow
                    current.uvIndex < 6 -> strings.uvModerate
                    current.uvIndex < 8 -> strings.uvHigh
                    else -> strings.uvVeryHigh
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Clouds & Precipitation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = strings.metricCloudsTitle,
                icon = Icons.Rounded.WbCloudy,
                value = "${current.cloudCoverPercent}%",
                subtitle = when {
                    current.cloudCoverPercent < 20 -> strings.cloudsClear
                    current.cloudCoverPercent < 70 -> strings.cloudsPartly
                    else -> strings.cloudsOvercast
                },
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = strings.metricPrecipTitle,
                icon = Icons.Rounded.WaterDrop,
                value = "${current.precipitationMm} ${strings.mmUnit}",
                subtitle = if (todayForecast != null) {
                    "${strings.precipChancePrefix} ${todayForecast.precipitationProbMax}%"
                } else strings.nowLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Sunrise & Sunset
        if (todayForecast != null && todayForecast.sunrise.isNotBlank()) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x22FBBF24), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WbSunny,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.sunriseTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = todayForecast.sunrise,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(Color(0x33FFFFFF))
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x22F97316), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WbTwilight,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.sunsetTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = todayForecast.sunset,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    icon: ImageVector,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 0.5.sp
                    )
                }
                badge?.invoke()
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 15.sp
            )
        }
    }
}
