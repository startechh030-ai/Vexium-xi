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

/**
 * Obris splash screen — pure Compose Canvas.
 * Lightweight, works on all screen sizes.
 *
 * Phase 0: Black
 * Phase 1: Logo scales up
 * Phase 2: Orange flash
 * Phase 3: Logo slides left, text appears
 * Phase 4: Background + sweep
 * Phase 5: Fade out
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
    val bgAlpha = remember { Animatable(0f) }
    val sweepX = remember { Animatable(0f) }
    val fadeOut = remember { Animatable(1f) }
    val loadingAlpha = remember { Animatable(0f) }

    // ── Timeline ──
    LaunchedEffect(Unit) {
        try {
            delay(300)

            // Phase 1: Logo appears
            phase = 1
            logoAlpha.animateTo(1f, tween(200))
            logoScale.animateTo(1f, tween(400))
            delay(100)
            logoScale.animateTo(2.5f, tween(400))

            // Phase 2: Flash
            phase = 2
            flashAlpha.animateTo(0.8f, tween(200))
            delay(150)
            flashAlpha.animateTo(0f, tween(300))
            logoScale.animateTo(0.8f, tween(300))

            // Phase 3: Logo left + text
            phase = 3
            logoOffsetX.animateTo(-1f, tween(350))
            textAlpha.animateTo(1f, tween(300))
            delay(200)

            // Phase 4: Bg + sweep
            phase = 4
            bgAlpha.animateTo(1f, tween(400))
            loadingAlpha.animateTo(0.4f, tween(300))
            sweepX.animateTo(1f, tween(1200, easing = LinearEasing))

            // Phase 5: Hold + fade
            delay(400)
            phase = 5
            fadeOut.animateTo(0f, tween(500))

            onSplashFinished()
        } catch (e: Exception) {
            // If any animation fails, still navigate
            onSplashFinished()
        }
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0608)),
    ) {
        // ── All Canvas drawing ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val fo = fadeOut.value

            // Dark bg transition
            if (bgAlpha.value > 0f) {
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF1A0820).copy(alpha = bgAlpha.value * fo),
                            Color(0xFF120610).copy(alpha = bgAlpha.value * fo),
                        ),
                        start = Offset.Zero, end = Offset(w, h),
                    ),
                    size = Size(w, h),
                )
            }

            // Sweep light beam
            if (sweepX.value > 0f && phase >= 4) {
                val sx = -w * 0.2f + w * 1.4f * sweepX.value
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, OrangeBright.copy(alpha = 0.06f * fo), Color.White.copy(alpha = 0.08f * fo), OrangeBright.copy(alpha = 0.06f * fo), Color.Transparent),
                        startX = sx - w * 0.1f, endX = sx + w * 0.1f,
                    ),
                    size = Size(w, h),
                )
            }

            // Flash
            if (flashAlpha.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(OrangeLight.copy(alpha = flashAlpha.value * fo), OrangeBright.copy(alpha = flashAlpha.value * 0.4f * fo), Color.Transparent),
                        center = Offset(cx, cy), radius = w * 0.6f,
                    ),
                    radius = w * 0.6f, center = Offset(cx, cy),
                )
            }

            // Diamond logo
            if (logoAlpha.value > 0f) {
                val s = minOf(w, h) * 0.10f * logoScale.value
                val lx = cx + logoOffsetX.value * w * 0.18f
                drawDiamond(lx, cy, s, logoAlpha.value * fo, if (phase == 2) 0.8f else 0.2f)
            }
        }

        // ── "OBRIS" text ──
        if (textAlpha.value > 0f) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "OBRIS",
                    style = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = textAlpha.value * fadeOut.value),
                        letterSpacing = 8.sp,
                    ),
                )
            }
        }

        // ── "Loading..." ──
        if (loadingAlpha.value > 0f) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = "Loading...",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = OrangeBright.copy(alpha = loadingAlpha.value * fadeOut.value),
                        letterSpacing = 2.sp,
                    ),
                    modifier = Modifier.padding(bottom = 28.dp),
                )
            }
        }
    }
}

/** Draw the Obris diamond logo */
private fun DrawScope.drawDiamond(cx: Float, cy: Float, s: Float, alpha: Float, glow: Float) {
    // Glow
    if (glow > 0.05f) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(OrangeBright.copy(alpha = 0.10f * glow * alpha), Color.Transparent),
                center = Offset(cx, cy), radius = s * 2f,
            ),
            radius = s * 2f, center = Offset(cx, cy),
        )
    }

    // Shape
    val path = Path().apply {
        moveTo(cx, cy - s)
        lineTo(cx + s * 0.85f, cy)
        lineTo(cx, cy + s)
        lineTo(cx - s * 0.85f, cy)
        close()
    }

    // Orange gradient fill
    drawPath(
        path,
        brush = Brush.linearGradient(
            listOf(OrangeLight.copy(alpha = alpha), OrangeBright.copy(alpha = alpha), OrangeDark.copy(alpha = alpha)),
            start = Offset(cx - s, cy - s), end = Offset(cx + s, cy + s),
        ),
    )

    // Cyan top-left edge
    val edge = Path().apply { moveTo(cx, cy - s); lineTo(cx - s * 0.85f, cy) }
    drawPath(edge, CyanAccent.copy(alpha = alpha * 0.5f), style = Stroke(2f, cap = StrokeCap.Round))

    // Inner fold (3D effect)
    val fold = Path().apply {
        moveTo(cx - s * 0.1f, cy - s * 0.35f)
        lineTo(cx + s * 0.3f, cy + s * 0.1f)
        lineTo(cx, cy + s * 0.4f)
        lineTo(cx - s * 0.3f, cy)
        close()
    }
    drawPath(fold, Color(0xFF1A0A00).copy(alpha = alpha * 0.4f))
}
