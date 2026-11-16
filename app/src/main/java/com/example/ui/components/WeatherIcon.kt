package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SkyType
import com.example.data.model.WeatherCondition

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tintOverride: Color? = null
) {
    val (icon, color) = when (condition.skyType) {
        SkyType.CLEAR_DAY -> Icons.Rounded.WbSunny to Color(0xFFFFB300)
        SkyType.CLEAR_NIGHT -> Icons.Rounded.NightlightRound to Color(0xFFFDE047)
        SkyType.PARTLY_CLOUDY_DAY -> Icons.Rounded.WbCloudy to Color(0xFFFFD54F)
        SkyType.PARTLY_CLOUDY_NIGHT -> Icons.Rounded.Cloud to Color(0xFFCBD5E1)
        SkyType.OVERCAST -> Icons.Rounded.Cloud to Color(0xFF94A3B8)
        SkyType.FOG -> Icons.Rounded.Air to Color(0xFFA1A1AA)
        SkyType.RAIN -> Icons.Rounded.WaterDrop to Color(0xFF38BDF8)
        SkyType.HEAVY_RAIN -> Icons.Rounded.Thunderstorm to Color(0xFF0284C7)
        SkyType.SNOW -> Icons.Rounded.AcUnit to Color(0xFFE2E8F0)
        SkyType.THUNDERSTORM -> Icons.Rounded.Bolt to Color(0xFFFBBF24)
    }

    Icon(
        imageVector = icon,
        contentDescription = condition.nameUk,
        tint = tintOverride ?: color,
        modifier = modifier.size(size)
    )
}
