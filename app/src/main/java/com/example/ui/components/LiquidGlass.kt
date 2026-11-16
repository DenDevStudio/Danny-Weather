package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.SkyType
import kotlin.math.cos
import kotlin.math.sin

val LocalFramelessAdminMode = compositionLocalOf { false }
val LocalAppAccentColor = compositionLocalOf { Color(0xFFFFD54F) }
val LocalCardCornerRadius = compositionLocalOf { 26.dp }
val LocalGlassOpacity = compositionLocalOf { 1.0f }
val LocalAnimateLiquidBackground = compositionLocalOf { true }

fun Modifier.glassBorder(
    width: Dp,
    color: Color,
    shape: Shape,
    frameless: Boolean
): Modifier = if (frameless) this else this.border(width = width, color = color, shape = shape)

fun Modifier.glassBorder(
    width: Dp,
    brush: Brush,
    shape: Shape,
    frameless: Boolean
): Modifier = if (frameless) this else this.border(width = width, brush = brush, shape = shape)

private val LightLiquidGlassFill = Brush.verticalGradient(
    colors = listOf(
        Color(0x1CFFFFFF),
        Color(0x180C2B54),
        Color(0x220A2344)
    )
)

private val PureLiquidGlassFill = Brush.verticalGradient(
    colors = listOf(
        Color(0x26FFFFFF),
        Color(0x220D315E),
        Color(0x2E0A264A)
    )
)

private val RichLiquidGlassFill = Brush.verticalGradient(
    colors = listOf(
        Color(0x34FFFFFF),
        Color(0x330F376A),
        Color(0x420A2447)
    )
)

private val SpecularGlassBorder = Brush.linearGradient(
    colors = listOf(
        Color(0x99FFFFFF),
        Color(0x44FFFFFF),
        Color(0x22FFFFFF),
        Color(0x66FFFFFF)
    ),
    start = Offset(0f, 0f),
    end = Offset(400f, 800f)
)

private val TopSpecularBrush = Brush.horizontalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0x66FFFFFF),
        Color(0xCCFFFFFF),
        Color(0x66FFFFFF),
        Color.Transparent
    )
)

/**
 * High-Performance Animated Living Liquid Orbs:
 * Pre-caches all gradient shaders once via drawWithCache and avoids heavy
 * per-frame fullscreen RenderEffect blur passes, keeping UI silky-smooth at 60/120 FPS
 * with zero per-frame object allocations or recompositions.
 */
@Composable
fun LiquidBackgroundOrbs(
    skyType: SkyType = SkyType.CLEAR_DAY,
    modifier: Modifier = Modifier
) {
    val animateEnabled = LocalAnimateLiquidBackground.current
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_transition_v2")

    val orbitAngle1State = if (animateEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orbitAngle1"
        )
    } else null

    val orbitAngle2State = if (animateEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orbitAngle2"
        )
    } else null

    val pulseAlphaState = if (animateEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0.78f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(5500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else null

    val isSunny = skyType == SkyType.CLEAR_DAY || skyType == SkyType.PARTLY_CLOUDY_DAY

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width.coerceAtLeast(1f)
                val h = size.height.coerceAtLeast(1f)

                val orb1Radius = w * 0.56f
                val orb2Radius = w * 0.58f
                val orb3Radius = w * 0.50f

                val orb1Brush = if (isSunny) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FFD54F),
                            Color(0x2CF59E0B),
                            Color(0x0CF59E0B),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.72f, h * 0.22f),
                        radius = orb1Radius
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x4D1D4ED8),
                            Color(0x281E3A8A),
                            Color(0x0A1E3A8A),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.72f, h * 0.22f),
                        radius = orb1Radius
                    )
                }

                val orb2Brush = if (isSunny) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x4D38BDF8),
                            Color(0x240284C7),
                            Color(0x080284C7),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.26f, h * 0.48f),
                        radius = orb2Radius
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x444338CA),
                            Color(0x20312E81),
                            Color(0x06312E81),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.28f, h * 0.48f),
                        radius = orb2Radius
                    )
                }

                val orb3Brush = if (isSunny) {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x402DD4BF),
                            Color(0x180D9488),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.65f, h * 0.74f),
                        radius = orb3Radius
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x3838BDF8),
                            Color(0x180284C7),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.65f, h * 0.74f),
                        radius = orb3Radius
                    )
                }

                onDrawBehind {
                    val a1 = (orbitAngle1State?.value ?: 0.6f).toDouble()
                    val a2 = (orbitAngle2State?.value ?: 1.1f).toDouble()
                    val alpha = pulseAlphaState?.value ?: 0.9f

                    val dx1 = sin(a1).toFloat() * (w * 0.12f)
                    val dy1 = cos(a1).toFloat() * (h * 0.06f)
                    translate(left = dx1, top = dy1) {
                        drawCircle(
                            brush = orb1Brush,
                            radius = orb1Radius,
                            center = Offset(w * 0.72f, h * 0.22f),
                            alpha = alpha
                        )
                    }

                    val dx2 = cos(a2).toFloat() * (w * 0.13f)
                    val dy2 = sin(a2).toFloat() * (h * 0.08f)
                    translate(left = dx2, top = dy2) {
                        drawCircle(
                            brush = orb2Brush,
                            radius = orb2Radius,
                            center = Offset(w * 0.26f, h * 0.48f),
                            alpha = alpha
                        )
                    }

                    val dx3 = sin(a1 + 3.14).toFloat() * (w * 0.10f)
                    val dy3 = cos(a1 + 3.14).toFloat() * (h * 0.06f)
                    translate(left = dx3, top = dy3) {
                        drawCircle(
                            brush = orb3Brush,
                            radius = orb3Radius,
                            center = Offset(w * 0.65f, h * 0.74f),
                            alpha = alpha
                        )
                    }
                }
            }
    )
}

/**
 * Ultra-Transparent, High-FPS Liquid Glass Surface V2:
 * Uses pre-allocated top-level Brushes with zero per-card allocations during scroll,
 * while dynamically responding to user-selected glass opacity & corner radius.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    borderWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val frameless = LocalFramelessAdminMode.current
    val customRadius = LocalCardCornerRadius.current
    val glassOpacity = LocalGlassOpacity.current
    val effectiveShape = shape ?: RoundedCornerShape(customRadius)
    val fillBrush = when {
        glassOpacity <= 0.75f -> LightLiquidGlassFill
        glassOpacity >= 1.25f -> RichLiquidGlassFill
        else -> PureLiquidGlassFill
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(effectiveShape)
            .background(brush = fillBrush)
            .glassBorder(
                width = borderWidth,
                brush = SpecularGlassBorder,
                shape = effectiveShape,
                frameless = frameless
            )
            .then(
                if (frameless) {
                    Modifier
                } else {
                    Modifier.drawBehind {
                        drawLine(
                            brush = TopSpecularBrush,
                            start = Offset(size.width * 0.08f, 1.2f),
                            end = Offset(size.width * 0.92f, 1.2f),
                            strokeWidth = 1.6f
                        )
                    }
                }
            )
    ) {
        content()
    }
}
