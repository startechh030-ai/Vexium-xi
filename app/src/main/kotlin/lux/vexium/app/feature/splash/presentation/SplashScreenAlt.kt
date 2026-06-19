package lux.vexium.app.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Splash Screen Alt — Sphere with warm lightning sweep.
 *
 * Pure black → sphere appears → warm light sweeps left to right
 * along the sphere edge → "Vexium" fades in → fade out → navigate.
 */
@Composable
fun SplashScreenAlt(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableStateOf(0) }

    // Sweep progress: 0 = light at left edge, 1 = light at right edge
    val sweepProgress = remember { Animatable(0f) }

    // Sphere fade-in
    var sphereTargetAlpha by remember { mutableFloatStateOf(0f) }
    val sphereAlpha by animateFloatAsState(
        targetValue = sphereTargetAlpha,
        animationSpec = tween(800),
        label = "sphere_alpha",
    )

    // Text fade
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(
        targetValue = textTargetAlpha,
        animationSpec = tween(1000),
        label = "text_alpha",
    )

    // Screen fade-out
    val screenAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Phase 0: Pure black (0.5s)
        delay(500)

        // Phase 1: Sphere fades in
        phase = 1
        sphereTargetAlpha = 1f
        delay(800)

        // Phase 2: Light sweeps left → right
        phase = 2
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing),
        )
        delay(200)

        // Phase 3: Text fades in
        phase = 3
        textTargetAlpha = 1f
        delay(1200)

        // Phase 4: Fade out
        phase = 4
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(700),
        )

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // Sphere + sweep
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            if (phase >= 1) {
                drawSplashSphere(
                    sphereAlpha = sphereAlpha * screenAlpha.value,
                    sweepProgress = if (phase >= 2) sweepProgress.value else 0f,
                    screenAlpha = screenAlpha.value,
                )
            }
        }

        // Text
        if (phase >= 3) {
            Text(
                text = "Vexium",
                style = TextStyle(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = textAlpha * screenAlpha.value),
                    letterSpacing = 4.sp,
                ),
            )
        }
    }
}

private fun DrawScope.drawSplashSphere(
    sphereAlpha: Float,
    sweepProgress: Float,
    screenAlpha: Float,
) {
    val w = size.width
    val h = size.height

    val sphereRadius = w * 0.85f
    val cx = w / 2f
    val cy = h / 2f + sphereRadius * 0.25f  // sphere center below screen center

    // ── Sphere body (very dark, barely visible) ──
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFF050505).copy(alpha = sphereAlpha),
                0.90f to Color(0xFF030303).copy(alpha = sphereAlpha),
                0.97f to Color(0xFF080808).copy(alpha = sphereAlpha),
                1.0f to Color(0xFF0A0A0A).copy(alpha = sphereAlpha),
            ),
            center = Offset(cx, cy),
            radius = sphereRadius,
        ),
        radius = sphereRadius,
        center = Offset(cx, cy),
    )

    // ── Subtle edge ring (always visible, dim) ──
    for (i in 1..3) {
        val sw = i * 3f
        val a = (0.04f - i * 0.01f).coerceAtLeast(0.005f) * sphereAlpha
        drawCircle(
            color = Color.White.copy(alpha = a),
            radius = sphereRadius + sw,
            center = Offset(cx, cy),
            style = Stroke(width = sw),
        )
    }

    drawCircle(
        color = Color.White.copy(alpha = 0.06f * sphereAlpha),
        radius = sphereRadius,
        center = Offset(cx, cy),
        style = Stroke(width = 1f),
    )

    // ── Warm lightning sweep ──
    if (sweepProgress > 0f) {
        // The light travels along the top arc of the sphere from left to right.
        // sweepProgress 0→1 maps to angle from ~210° (left) to ~330° (right)
        // on the circle (measuring from 3 o'clock, counter-clockwise for top)
        val startAngle = 210f  // left side of visible arc
        val endAngle = 330f    // right side of visible arc
        val currentAngle = startAngle + (endAngle - startAngle) * sweepProgress
        val angleRad = currentAngle * PI.toFloat() / 180f

        val lightX = cx + sphereRadius * cos(angleRad)
        val lightY = cy + sphereRadius * sin(angleRad)

        // Warm colors
        val warmWhite = Color(0xFFFFF8F0)
        val warmGold = Color(0xFFFFE4B5)

        // Wide soft glow around the light point
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to warmWhite.copy(alpha = 0.35f * screenAlpha),
                    0.15f to warmGold.copy(alpha = 0.18f * screenAlpha),
                    0.4f to warmGold.copy(alpha = 0.05f * screenAlpha),
                    1.0f to Color.Transparent,
                ),
                center = Offset(lightX, lightY),
                radius = sphereRadius * 0.35f,
            ),
            radius = sphereRadius * 0.35f,
            center = Offset(lightX, lightY),
        )

        // Bright core
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.70f * screenAlpha),
                    0.15f to warmWhite.copy(alpha = 0.40f * screenAlpha),
                    0.4f to warmWhite.copy(alpha = 0.08f * screenAlpha),
                    1.0f to Color.Transparent,
                ),
                center = Offset(lightX, lightY),
                radius = sphereRadius * 0.12f,
            ),
            radius = sphereRadius * 0.12f,
            center = Offset(lightX, lightY),
        )

        // Trail: fading glow behind the light (previous positions)
        val trailLength = 0.15f
        val trailSteps = 8
        for (t in 1..trailSteps) {
            val trailProgress = (sweepProgress - trailLength * t / trailSteps).coerceAtLeast(0f)
            val trailAngle = startAngle + (endAngle - startAngle) * trailProgress
            val trailRad = trailAngle * PI.toFloat() / 180f
            val tx = cx + sphereRadius * cos(trailRad)
            val ty = cy + sphereRadius * sin(trailRad)
            val trailAlpha = (1f - t.toFloat() / trailSteps) * 0.12f * screenAlpha

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to warmGold.copy(alpha = trailAlpha),
                        0.5f to warmGold.copy(alpha = trailAlpha * 0.3f),
                        1.0f to Color.Transparent,
                    ),
                    center = Offset(tx, ty),
                    radius = sphereRadius * 0.08f,
                ),
                radius = sphereRadius * 0.08f,
                center = Offset(tx, ty),
            )
        }

        // Illuminate the sphere edge near the light (bright arc segment)
        val arcSpread = 25f // degrees of arc lit up around the light
        for (deg in -arcSpread.toInt()..arcSpread.toInt()) {
            val a = currentAngle + deg
            val aRad = a * PI.toFloat() / 180f
            val px = cx + sphereRadius * cos(aRad)
            val py = cy + sphereRadius * sin(aRad)
            val intensity = (1f - (kotlin.math.abs(deg) / arcSpread)) * 0.25f * screenAlpha

            drawCircle(
                color = warmWhite.copy(alpha = intensity),
                radius = 3f,
                center = Offset(px, py),
            )
        }
    }
}
