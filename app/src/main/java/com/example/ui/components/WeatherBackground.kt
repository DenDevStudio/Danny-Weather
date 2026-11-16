package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.SkyType
import com.example.ui.theme.ClearDayGradient
import com.example.ui.theme.ClearNightGradient
import com.example.ui.theme.FogGradient
import com.example.ui.theme.HeavyRainGradient
import com.example.ui.theme.OvercastGradient
import com.example.ui.theme.PartlyCloudyDayGradient
import com.example.ui.theme.PartlyCloudyNightGradient
import com.example.ui.theme.RainGradient
import com.example.ui.theme.SnowGradient
import com.example.ui.theme.ThunderstormGradient

@Composable
fun WeatherBackground(
    skyType: SkyType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = when (skyType) {
        SkyType.CLEAR_DAY -> ClearDayGradient
        SkyType.CLEAR_NIGHT -> ClearNightGradient
        SkyType.PARTLY_CLOUDY_DAY -> PartlyCloudyDayGradient
        SkyType.PARTLY_CLOUDY_NIGHT -> PartlyCloudyNightGradient
        SkyType.OVERCAST -> OvercastGradient
        SkyType.FOG -> FogGradient
        SkyType.RAIN -> RainGradient
        SkyType.HEAVY_RAIN -> HeavyRainGradient
        SkyType.SNOW -> SnowGradient
        SkyType.THUNDERSTORM -> ThunderstormGradient
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Pre-cached decorative atmospheric backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width.coerceAtLeast(1f)
                    val height = size.height.coerceAtLeast(1f)
                    val clearDayBrush = Brush.radialGradient(
                        colors = listOf(Color(0x30FFF7ED), Color(0x18FBBF24), Color.Transparent),
                        center = Offset(width * 0.85f, height * 0.15f),
                        radius = width * 0.55f
                    )
                    val ambientBrush = Brush.radialGradient(
                        colors = listOf(Color(0x1467E8F9), Color.Transparent),
                        center = Offset(width * 0.5f, height * 0.2f),
                        radius = width * 0.65f
                    )

                    onDrawBehind {
                        when (skyType) {
                            SkyType.CLEAR_DAY -> {
                                drawCircle(
                                    brush = clearDayBrush,
                                    radius = width * 0.6f,
                                    center = Offset(width * 0.85f, height * 0.15f)
                                )
                            }
                            SkyType.CLEAR_NIGHT -> {
                                val starPositions = listOf(
                                    Offset(width * 0.15f, height * 0.12f),
                                    Offset(width * 0.35f, height * 0.08f),
                                    Offset(width * 0.75f, height * 0.18f),
                                    Offset(width * 0.88f, height * 0.06f),
                                    Offset(width * 0.55f, height * 0.22f),
                                    Offset(width * 0.22f, height * 0.30f),
                                    Offset(width * 0.65f, height * 0.35f)
                                )
                                starPositions.forEach { pos ->
                                    drawCircle(
                                        color = Color(0xAAFFFFFF),
                                        radius = 2.5f,
                                        center = pos
                                    )
                                }
                            }
                            SkyType.RAIN, SkyType.HEAVY_RAIN -> {
                                val drops = listOf(
                                    Offset(width * 0.1f, height * 0.1f) to Offset(width * 0.08f, height * 0.14f),
                                    Offset(width * 0.3f, height * 0.2f) to Offset(width * 0.28f, height * 0.25f),
                                    Offset(width * 0.55f, height * 0.12f) to Offset(width * 0.53f, height * 0.17f),
                                    Offset(width * 0.8f, height * 0.22f) to Offset(width * 0.78f, height * 0.28f),
                                    Offset(width * 0.9f, height * 0.08f) to Offset(width * 0.88f, height * 0.13f),
                                    Offset(width * 0.2f, height * 0.4f) to Offset(width * 0.18f, height * 0.45f),
                                    Offset(width * 0.7f, height * 0.45f) to Offset(width * 0.68f, height * 0.5f)
                                )
                                drops.forEach { (start, end) ->
                                    drawLine(
                                        color = Color(0x33BAE6FD),
                                        start = start,
                                        end = end,
                                        strokeWidth = 2f
                                    )
                                }
                            }
                            SkyType.SNOW -> {
                                val flakes = listOf(
                                    Offset(width * 0.18f, height * 0.15f) to 3f,
                                    Offset(width * 0.45f, height * 0.1f) to 4.5f,
                                    Offset(width * 0.72f, height * 0.2f) to 3f,
                                    Offset(width * 0.85f, height * 0.12f) to 5f,
                                    Offset(width * 0.3f, height * 0.35f) to 4f,
                                    Offset(width * 0.65f, height * 0.4f) to 3.5f
                                )
                                flakes.forEach { (pos, r) ->
                                    drawCircle(
                                        color = Color(0x77FFFFFF),
                                        radius = r,
                                        center = pos
                                    )
                                }
                            }
                            else -> {
                                drawCircle(
                                    brush = ambientBrush,
                                    radius = width * 0.7f,
                                    center = Offset(width * 0.5f, height * 0.2f)
                                )
                            }
                        }
                    }
                }
        )

        // Dynamic living Liquid Glass orbs flowing smoothly at 120 FPS
        LiquidBackgroundOrbs(
            skyType = skyType,
            modifier = Modifier.fillMaxSize()
        )

        content()
    }
}
