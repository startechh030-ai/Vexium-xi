package lux.obris.app.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Splash — big "O" logo centered on black, glitches, then navigates.
 * Similar to Free Fire's "F" splash. Pure Canvas, no video.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val glitchIntensity = remember { Animatable(0f) }
    val fadeOut = remember { Animatable(1f) }

    // Glitch offsets — randomized each frame via recomposition
    val glitchSeed = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        // Fade in
        logoAlpha.animateTo(1f, tween(400))
        delay(300)

        // Glitch burst 1
        glitchIntensity.animateTo(1f, tween(80))
        glitchSeed.animateTo(1f, tween(80))
        glitchIntensity.animateTo(0f, tween(60))
        delay(150)

        // Glitch burst 2
        glitchIntensity.animateTo(0.7f, tween(50))
        glitchSeed.animateTo(2f, tween(50))
        glitchIntensity.animateTo(0f, tween(80))
        delay(100)

        // Glitch burst 3 — heavy
        glitchIntensity.animateTo(1f, tween(40))
        glitchSeed.animateTo(3f, tween(40))
        delay(60)
        glitchIntensity.animateTo(0.3f, tween(30))
        glitchSeed.animateTo(4f, tween(30))
        glitchIntensity.animateTo(1f, tween(40))
        glitchSeed.animateTo(5f, tween(40))
        glitchIntensity.animateTo(0f, tween(100))

        // Hold clean
        delay(400)

        // Fade out
        fadeOut.animateTo(0f, tween(400))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val cx = w / 2f
            val cy = h / 2f
            val alpha = logoAlpha.value * fadeOut.value
            val glitch = glitchIntensity.value
            val seed = glitchSeed.value

            if (alpha <= 0f) return@Canvas

            // Draw the "O" with glitch effect
            drawObrisO(cx, cy, minOf(w, h) * 0.18f, alpha, glitch, seed)
        }
    }
}

/**
 * Draw a bold stylized "O" — the Obris brand mark.
 * With glitch: RGB split, horizontal slice displacement.
 */
private fun DrawScope.drawObrisO(
    cx: Float, cy: Float, radius: Float,
    alpha: Float, glitch: Float, seed: Float,
) {
    val strokeW = radius * 0.18f
    val color = Color(0xFFFF8C00) // Brand orange

    // ── Glitch: RGB channel split ──
    if (glitch > 0.1f) {
        val offset = glitch * radius * 0.15f
        val rng = Random(seed.toInt())
        val dx1 = (rng.nextFloat() - 0.5f) * offset * 2f
        val dy1 = (rng.nextFloat() - 0.5f) * offset
        val dx2 = (rng.nextFloat() - 0.5f) * offset * 2f
        val dy2 = (rng.nextFloat() - 0.5f) * offset

        // Red channel
        drawCircle(
            color = Color.Red.copy(alpha = alpha * glitch * 0.5f),
            radius = radius,
            center = Offset(cx + dx1, cy + dy1),
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )
        // Cyan channel
        drawCircle(
            color = Color.Cyan.copy(alpha = alpha * glitch * 0.4f),
            radius = radius,
            center = Offset(cx + dx2, cy + dy2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
        )

        // Horizontal glitch slices
        val sliceCount = (glitch * 6).toInt().coerceAtLeast(1)
        for (i in 0 until sliceCount) {
            val sliceY = cy - radius + (2f * radius * rng.nextFloat())
            val sliceH = radius * 0.08f * rng.nextFloat()
            val sliceDx = (rng.nextFloat() - 0.5f) * radius * 0.4f * glitch
            drawRect(
                color = color.copy(alpha = alpha * 0.6f),
                topLeft = Offset(cx - radius - strokeW + sliceDx, sliceY),
                size = androidx.compose.ui.geometry.Size(radius * 2f + strokeW * 2f, sliceH),
            )
        }
    }

    // ── Main "O" ring ──
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = strokeW, cap = StrokeCap.Round),
    )

    // ── Inner accent slash (like the Obris logo mark) ──
    val slashPath = Path().apply {
        moveTo(cx + radius * 0.25f, cy - radius * 0.55f)
        lineTo(cx - radius * 0.15f, cy + radius * 0.55f)
    }
    drawPath(
        slashPath,
        color = color.copy(alpha = alpha * 0.9f),
        style = Stroke(width = strokeW * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // ── Subtle glow behind ──
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.08f),
                Color.Transparent,
            ),
            center = Offset(cx, cy),
            radius = (radius * 2.5f).coerceAtLeast(10f),
        ),
        radius = (radius * 2.5f).coerceAtLeast(10f),
        center = Offset(cx, cy),
    )
}
