package lux.obris.app.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Brand colors ──
private val OrangeBright = Color(0xFFFF8C00)
private val OrangeLight = Color(0xFFFFAA33)
private val OrangeDark = Color(0xFFCC6600)
private val CyanAccent = Color(0xFF00E5FF)
private val DeepDark = Color(0xFF0A0608)

/**
 * Obris splash screen — pure Compose Canvas animation.
 *
 * Phase 0: Black
 * Phase 1: Diamond logo scales up from center
 * Phase 2: Orange energy burst
 * Phase 3: Logo slides left, "OBRIS" text appears
 * Phase 4: Dark bg with light sweep + loading text
 * Phase 5: Hold
 * Phase 6: Fade out → navigate
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableIntStateOf(0) }

    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val logoOffsetX = remember { Animatable(0f) }
    val bgTransition = remember { Animatable(0f) }
    val sweepProgress = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    val loadingAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)

        // Phase 1: Logo appears + scales up
        phase = 1
        logoAlpha.animateTo(1f, tween(200))
        logoScale.animateTo(1f, tween(400, easing = LinearEasing))
        delay(100)
        logoScale.animateTo(2.8f, tween(400, easing = LinearEasing))

        // Phase 2: Flash
        phase = 2
        flashAlpha.animateTo(0.9f, tween(200))
        delay(200)
        flashAlpha.animateTo(0f, tween(300))
        logoScale.animateTo(0.8f, tween(300))

        // Phase 3: Logo left + text
        phase = 3
        logoOffsetX.animateTo(-1f, tween(350, easing = LinearEasing))
        textAlpha.animateTo(1f, tween(300))
        delay(200)

        // Phase 4: Bg + sweep
        phase = 4
        bgTransition.animateTo(1f, tween(400))
        loadingAlpha.animateTo(0.4f, tween(300))
        sweepProgress.animateTo(1f, tween(1200, easing = LinearEasing))

        // Phase 5: Hold
        phase = 5
        delay(500)

        // Phase 6: Fade
        phase = 6
        screenAlpha.animateTo(0f, tween(500))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDark),
    ) {
        // ── Canvas: all animated graphics ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val a = screenAlpha.value

            // Background transition
            if (bgTransition.value > 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A0820).copy(alpha = bgTransition.value * a),
                            Color(0xFF120610).copy(alpha = bgTransition.value * a),
                            Color(0xFF1A0A08).copy(alpha = bgTransition.value * 0.5f * a),
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h),
                    ),
                    size = Size(w, h),
                )
            }

            // Watermark lines
            if (bgTransition.value > 0.5f) {
                val wAlpha = 0.05f * a
                val spacing = w * 0.08f
                for (i in -3..3) {
                    val x = cx + i * spacing
                    drawLine(
                        OrangeBright.copy(alpha = wAlpha),
                        Offset(x - w * 0.02f, cy - w * 0.04f),
                        Offset(x + w * 0.02f, cy + w * 0.04f),
                        strokeWidth = w * 0.006f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            // Light sweep
            if (sweepProgress.value > 0f && phase >= 4) {
                val sweepX = -w * 0.3f + (w * 1.6f) * sweepProgress.value
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            OrangeBright.copy(alpha = 0.06f * a),
                            Color.White.copy(alpha = 0.10f * a),
                            OrangeBright.copy(alpha = 0.06f * a),
                            Color.Transparent,
                        ),
                        startX = sweepX - w * 0.12f,
                        endX = sweepX + w * 0.12f,
                    ),
                    size = Size(w, h),
                )
            }

            // Flash
            if (flashAlpha.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OrangeLight.copy(alpha = flashAlpha.value * a),
                            OrangeBright.copy(alpha = flashAlpha.value * 0.5f * a),
                            Color.Transparent,
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.7f,
                    ),
                    radius = w * 0.7f,
                    center = Offset(cx, cy),
                )
            }

            // Logo
            if (logoAlpha.value > 0f) {
                val scale = logoScale.value
                val offsetX = logoOffsetX.value * w * 0.18f
                val logoSize = minOf(w, h) * 0.10f * scale
                drawObrisLogo(cx + offsetX, cy, logoSize, logoAlpha.value * a, if (phase == 2) 1f else 0.3f)
            }
        }

        // ── "OBRIS" text — centered in Box ──
        if (textAlpha.value > 0f) {
            Text(
                text = "OBRIS",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = textAlpha.value * screenAlpha.value),
                    letterSpacing = 8.sp,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ── "Loading..." at bottom ──
        if (loadingAlpha.value > 0f) {
            Text(
                text = "Loading...",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = OrangeBright.copy(alpha = loadingAlpha.value * screenAlpha.value),
                    letterSpacing = 2.sp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
            )
        }
    }
}

/** Obris diamond logo — orange gradient + cyan highlight + inner fold */
private fun DrawScope.drawObrisLogo(
    cx: Float, cy: Float, s: Float, alpha: Float, glowIntensity: Float,
) {
    // Glow
    if (glowIntensity > 0.1f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrangeBright.copy(alpha = 0.12f * glowIntensity * alpha),
                    Color.Transparent,
                ),
                center = Offset(cx, cy), radius = s * 2f,
            ),
            radius = s * 2f, center = Offset(cx, cy),
        )
    }

    // Diamond shape
    val diamond = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx + s * 0.85f, cy)
        lineTo(cx, cy + s)
        lineTo(cx - s * 0.85f, cy)
        close()
    }

    // Fill
    drawPath(
        diamond,
        brush = Brush.linearGradient(
            colors = listOf(OrangeLight.copy(alpha = alpha), OrangeBright.copy(alpha = alpha), OrangeDark.copy(alpha = alpha)),
            start = Offset(cx - s, cy - s), end = Offset(cx + s, cy + s),
        ),
    )

    // Cyan edge
    val edge = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx - s * 0.85f, cy)
    }
    drawPath(edge, CyanAccent.copy(alpha = alpha * 0.6f), style = Stroke(2.5f, cap = StrokeCap.Round))

    // Inner fold
    val fold = Path().apply {
        moveTo(cx - s * 0.15f, cy - s * 0.4f)
        lineTo(cx + s * 0.35f, cy + s * 0.1f)
        lineTo(cx, cy + s * 0.45f)
        lineTo(cx - s * 0.35f, cy)
        close()
    }
    drawPath(fold, Color(0xFF1A0A00).copy(alpha = alpha * 0.5f))
}
