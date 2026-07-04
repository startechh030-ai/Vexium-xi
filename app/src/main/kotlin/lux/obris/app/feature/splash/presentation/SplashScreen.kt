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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
private val WarmDark = Color(0xFF1A0E14)

/**
 * Obris splash screen — pure Compose Canvas, no video.
 * Inspired by Farlight 84 but with Obris orange/cyan brand.
 *
 * Timeline:
 *  Phase 0 (0.0-0.3s): Black screen
 *  Phase 1 (0.3-1.2s): Obris diamond logo scales up from center
 *  Phase 2 (1.2-1.8s): Logo pulses, orange energy burst fills screen
 *  Phase 3 (1.8-2.5s): Flash fades, logo shrinks to left, "OBRIS" text slides in
 *  Phase 4 (2.5-4.0s): Dark purple/orange bg, large watermark text, light sweep
 *  Phase 5 (4.0-4.5s): Hold final frame
 *  Phase 6 (4.5-5.0s): Fade out → navigate
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableIntStateOf(0) }

    // ── Animation values ──
    val logoScale = remember { Animatable(0f) }         // 0 = invisible, 1 = normal, 3 = huge
    val logoAlpha = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }        // Orange flash
    val textAlpha = remember { Animatable(0f) }         // "OBRIS" text
    val logoOffsetX = remember { Animatable(0f) }       // 0 = center, -1 = left
    val bgTransition = remember { Animatable(0f) }      // 0 = dark, 1 = purple/orange bg
    val sweepProgress = remember { Animatable(0f) }     // Light sweep across bg
    val watermarkAlpha = remember { Animatable(0f) }    // Large bg watermark
    val screenAlpha = remember { Animatable(1f) }       // Final fade out
    val loadingAlpha = remember { Animatable(0f) }      // "Loading..." text

    // ── Timeline ──
    LaunchedEffect(Unit) {
        // Phase 0: Black (0.3s)
        delay(300)

        // Phase 1: Logo scales up from nothing (0.3-1.2s)
        phase = 1
        logoAlpha.animateTo(1f, tween(200))
        logoScale.animateTo(1f, tween(400, easing = LinearEasing))
        delay(100)
        // Dramatic scale up
        logoScale.animateTo(2.8f, tween(400, easing = LinearEasing))

        // Phase 2: Orange energy flash (1.2-1.8s)
        phase = 2
        flashAlpha.animateTo(0.9f, tween(200))
        delay(200)
        flashAlpha.animateTo(0f, tween(300))
        logoScale.animateTo(0.8f, tween(300))

        // Phase 3: Logo slides left, text appears (1.8-2.5s)
        phase = 3
        logoOffsetX.animateTo(-1f, tween(350, easing = LinearEasing))
        textAlpha.animateTo(1f, tween(300))
        delay(200)

        // Phase 4: Background transition + watermark + sweep (2.5-4.0s)
        phase = 4
        bgTransition.animateTo(1f, tween(400))
        watermarkAlpha.animateTo(0.06f, tween(300))
        loadingAlpha.animateTo(0.4f, tween(300))
        sweepProgress.animateTo(1f, tween(1200, easing = LinearEasing))

        // Phase 5: Hold (4.0-4.5s)
        phase = 5
        delay(500)

        // Phase 6: Fade out (4.5-5.0s)
        phase = 6
        screenAlpha.animateTo(0f, tween(500))
        onSplashFinished()
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepDark),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val alpha = screenAlpha.value

            // ── Background ──
            // Dark → warm purple/orange gradient
            if (bgTransition.value > 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A0820).copy(alpha = bgTransition.value * alpha),
                            Color(0xFF120610).copy(alpha = bgTransition.value * alpha),
                            Color(0xFF1A0A08).copy(alpha = bgTransition.value * 0.5f * alpha),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h),
                    ),
                    size = Size(w, h),
                )
            }

            // ── Large watermark text "OBRIS" in bg ──
            if (watermarkAlpha.value > 0f) {
                drawWatermark(cx, cy, w, watermarkAlpha.value * alpha)
            }

            // ── Light sweep (diagonal beam moving left to right) ──
            if (sweepProgress.value > 0f && phase >= 4) {
                val sweepX = -w * 0.3f + (w * 1.6f) * sweepProgress.value
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            OrangeBright.copy(alpha = 0.08f * alpha),
                            Color.White.copy(alpha = 0.12f * alpha),
                            OrangeBright.copy(alpha = 0.08f * alpha),
                            Color.Transparent,
                        ),
                        startX = sweepX - w * 0.15f,
                        endX = sweepX + w * 0.15f,
                    ),
                    size = Size(w, h),
                )
            }

            // ── Orange energy flash ──
            if (flashAlpha.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            OrangeLight.copy(alpha = flashAlpha.value * alpha),
                            OrangeBright.copy(alpha = flashAlpha.value * 0.6f * alpha),
                            Color.Transparent,
                        ),
                        center = Offset(cx, cy),
                        radius = w * 0.8f,
                    ),
                    radius = w * 0.8f,
                    center = Offset(cx, cy),
                )
            }

            // ── Obris diamond logo ──
            if (logoAlpha.value > 0f) {
                val scale = logoScale.value
                val offsetX = logoOffsetX.value * w * 0.18f
                val logoSize = minOf(w, h) * 0.12f * scale
                val logoCx = cx + offsetX
                val logoCy = if (phase >= 3) cy else cy

                drawObrisLogo(
                    cx = logoCx,
                    cy = logoCy,
                    size = logoSize,
                    alpha = logoAlpha.value * alpha,
                    glowIntensity = if (phase == 2) 1f else 0.3f,
                )
            }
        }

        // ── "OBRIS" text (appears in phase 3+) ──
        if (textAlpha.value > 0f) {
            Text(
                text = "OBRIS",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = textAlpha.value * screenAlpha.value),
                    letterSpacing = 8.sp,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ── "Loading..." at bottom (phase 4+) ──
        if (loadingAlpha.value > 0f) {
            Text(
                text = "Loading...",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = OrangeBright.copy(alpha = loadingAlpha.value * screenAlpha.value),
                    letterSpacing = 2.sp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
            )
        }
    }
}

/**
 * Draw the Obris diamond logo — a stylized geometric diamond
 * with orange gradient fill and cyan edge highlights.
 */
private fun DrawScope.drawObrisLogo(
    cx: Float, cy: Float, size: Float,
    alpha: Float, glowIntensity: Float,
) {
    // ── Outer glow ──
    if (glowIntensity > 0.1f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    OrangeBright.copy(alpha = 0.15f * glowIntensity * alpha),
                    OrangeBright.copy(alpha = 0.05f * glowIntensity * alpha),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = size * 2f,
            ),
            radius = size * 2f,
            center = Offset(cx, cy),
        )
    }

    // ── Diamond shape ──
    val diamond = Path().apply {
        moveTo(cx, cy - size)          // Top
        lineTo(cx + size * 0.85f, cy)  // Right
        lineTo(cx, cy + size)          // Bottom
        lineTo(cx - size * 0.85f, cy)  // Left
        close()
    }

    // Fill with orange gradient
    drawPath(
        diamond,
        brush = Brush.linearGradient(
            colors = listOf(
                OrangeLight.copy(alpha = alpha),
                OrangeBright.copy(alpha = alpha),
                OrangeDark.copy(alpha = alpha),
            ),
            start = Offset(cx - size, cy - size),
            end = Offset(cx + size, cy + size),
        ),
    )

    // Cyan edge highlight (top-left edge)
    val highlight = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx - size * 0.85f, cy)
    }
    drawPath(
        highlight,
        color = CyanAccent.copy(alpha = alpha * 0.6f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round),
    )

    // Inner cut — gives it a 3D folded look
    val innerCut = Path().apply {
        moveTo(cx - size * 0.15f, cy - size * 0.4f)
        lineTo(cx + size * 0.35f, cy + size * 0.1f)
        lineTo(cx, cy + size * 0.45f)
        lineTo(cx - size * 0.35f, cy)
        close()
    }
    drawPath(
        innerCut,
        color = Color(0xFF1A0A00).copy(alpha = alpha * 0.5f),
    )
}

/**
 * Draw large "OBRIS" watermark text in the background.
 * Uses simple geometric shapes since Canvas can't draw text directly.
 * We approximate with angled lines for the futuristic feel.
 */
private fun DrawScope.drawWatermark(cx: Float, cy: Float, width: Float, alpha: Float) {
    // Large diagonal lines to simulate watermark text feel
    val color = OrangeBright.copy(alpha = alpha)
    val spacing = width * 0.08f

    for (i in -3..3) {
        val x = cx + i * spacing
        drawLine(
            color = color,
            start = Offset(x - width * 0.02f, cy - width * 0.04f),
            end = Offset(x + width * 0.02f, cy + width * 0.04f),
            strokeWidth = width * 0.008f,
            cap = StrokeCap.Round,
        )
    }
}


