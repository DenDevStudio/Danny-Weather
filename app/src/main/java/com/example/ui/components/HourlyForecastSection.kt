package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
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
import com.example.data.model.HourlyForecastItem
import com.example.ui.i18n.LocalAppStrings

@Composable
fun HourlyForecastSection(
    hourlyItems: List<HourlyForecastItem>,
    formatTemp: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAppAccentColor.current
    val strings = LocalAppStrings.current
    LiquidGlassCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.hourlyTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("hourly_forecast_row")
            ) {
                items(hourlyItems, key = { it.fullTime.ifBlank { it.timeLabel } }) { item ->
                    HourlyItemCard(item = item, formatTemp = formatTemp)
                }
            }
        }
    }
}

@Composable
private fun HourlyItemCard(
    item: HourlyForecastItem,
    formatTemp: (Double) -> String
) {
    val strings = LocalAppStrings.current
    val isNow = item.isNow || item.timeLabel == "Зараз"
    val frameless = LocalFramelessAdminMode.current
    val accentColor = LocalAppAccentColor.current
    Box(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isNow) accentColor.copy(alpha = 0.22f) else Color(0x18FFFFFF))
            .glassBorder(
                width = 1.dp,
                color = if (isNow) accentColor else Color(0x33FFFFFF),
                shape = RoundedCornerShape(18.dp),
                frameless = frameless
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isNow) strings.nowLabel else item.timeLabel,
                fontSize = 12.sp,
                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                color = if (isNow) accentColor else Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            WeatherIcon(
                condition = item.condition,
                size = 28.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Precipitation probability badge if > 0%
            if (item.precipitationProb > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = strings.precipChanceCd,
                        tint = Color(0xFF67E8F9),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.precipitationProb}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF67E8F9)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = formatTemp(item.tempCelsius),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
