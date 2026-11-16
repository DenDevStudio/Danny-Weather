package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.AirQualityInfo
import com.example.data.model.WeatherCodeMapper
import com.example.ui.i18n.LocalAppStrings

@Composable
fun AirQualityCard(
    airQuality: AirQualityInfo?,
    modifier: Modifier = Modifier
) {
    if (airQuality == null) return
    val frameless = LocalFramelessAdminMode.current
    val strings = LocalAppStrings.current
    val (levelLabel, adviceText) = WeatherCodeMapper.getAqiLevel(
        aqi = airQuality.europeanAqi,
        language = strings.language
    )

    val levelColor = when {
        airQuality.europeanAqi <= 20 -> Color(0xFF4ADE80) // Green
        airQuality.europeanAqi <= 40 -> Color(0xFF86EFAC) // Light green
        airQuality.europeanAqi <= 60 -> Color(0xFFFDE047) // Yellow
        airQuality.europeanAqi <= 80 -> Color(0xFFFB923C) // Orange
        else -> Color(0xFFF87171) // Red
    }

    val accentColor = LocalAppAccentColor.current

    LiquidGlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("air_quality_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.airQualityTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Level Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(levelColor.copy(alpha = 0.2f))
                        .glassBorder(1.dp, levelColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp), frameless)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = levelLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${airQuality.europeanAqi}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.aqiSubtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (airQuality.europeanAqi / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = levelColor,
                trackColor = Color(0x33FFFFFF)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pollutants detail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .glassBorder(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp), frameless)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutantItem("PM2.5", "${airQuality.pm25} ${strings.microgramUnit}")
                PollutantItem("PM10", "${airQuality.pm10} ${strings.microgramUnit}")
                PollutantItem(strings.ozoneLabel, "${Math.round(airQuality.ozone)} ${strings.microgramUnit}")
                PollutantItem("NO₂", "${Math.round(airQuality.nitrogenDioxide)} ${strings.microgramUnit}")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = adviceText,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PollutantItem(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
