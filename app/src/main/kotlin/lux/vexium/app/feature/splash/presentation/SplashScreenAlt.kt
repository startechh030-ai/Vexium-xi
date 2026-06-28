package lux.vexium.app.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import kotlin.random.Random

@Composable
fun SplashScreenAlt(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableStateOf(0) }

    // Sphere
    val sphereAlpha = remember { Animatable(0f) }

    // Sweep
    val sweepProgress = remember { Animatable(0f) }

    // Stars twinkle
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle",
    )

    // Text
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(textTargetAlpha, tween(1000), label = "text")

    // Screen fade
    val screenAlpha = remember { Animatable(1f) }

    // Generate stars once
    val stars = remember {
        List(60) {
            StarData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 0.5f,
                brightness = Random.nextFloat(),
                twinkleOffset = Random.nextFloat(),
            )
        }
    }

    LaunchedEffect(Unit) {
        // Phase 0: Stars appear + sphere fades in (0-2s)
        delay(500)
        sphereAlpha.animateTo(1f, tween(1500))

        // Phase 1: Warm light sweeps (2-4.5s)
        phase = 1
        sweepProgress.animateTo(1f, tween(2500, easing = LinearEasing))

        // Phase 2: Text fades in (4.5-6.5s)
        phase = 2
        textTargetAlpha = 1f
        delay(2000)

        // Phase 3: Fade out (6.5-7.3s)
        phase = 3
        screenAlpha.animateTo(0f, tween(800))

        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val alpha = screenAlpha.value

            // Stars
            drawStars(stars, twinkle, alpha)

            // Sphere
            if (sphereAlpha.value > 0f) {
                drawSplashSphere(
                    sphereAlpha = sphereAlpha.value * alpha,
                    sweepProgress = if (phase >= 1) sweepProgress.value else 0f,
                    alpha = alpha,
                )
            }
        }

        // Text
        if (phase >= 2) {
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

data class StarData(
    val x: Float, val y: Float, val size: Float,
    val brightness: Float, val twinkleOffset: Float,
)

private fun DrawScope.drawStars(stars: List<StarData>, twinkle: Float, alpha: Float) {
    val w = size.width; val h = size.height
    stars.forEach { star ->
        val t = ((twinkle + star.twinkleOffset) % 1f)
        val starAlpha = (star.brightness * 0.3f + t * 0.7f * star.brightness) * alpha * 0.6f
        drawCircle(
            color = Color.White.copy(alpha = starAlpha.coerceIn(0f, 1f)),
            radius = star.size,
            center = Offset(star.x * w, star.y * h),
        )
    }
}

private fun DrawScope.drawSplashSphere(sphereAlpha: Float, sweepProgress: Float, alpha: Float) {
    val w = size.width; val h = size.height
    val r = w * 0.85f
    val cx = w / 2f; val cy = h / 2f + r * 0.25f

    // Body
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFF030308).copy(alpha = sphereAlpha),
                0.92f to Color(0xFF020206).copy(alpha = sphereAlpha),
                0.97f to Color(0xFF0A0A12).copy(alpha = sphereAlpha),
                1f to Color(0xFF0E0E18).copy(alpha = sphereAlpha),
            ),
            center = Offset(cx, cy), radius = r,
        ),
        radius = r, center = Offset(cx, cy),
    )

    // Edge glow
    for (i in 1..3) {
        val sw = i * 3f
        val a = (0.04f - i * 0.01f).coerceAtLeast(0.005f) * sphereAlpha
        drawCircle(Color.White.copy(alpha = a), r + sw, Offset(cx, cy), style = Stroke(sw))
    }
    drawCircle(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to Color(0xFF5EB0EF).copy(alpha = 0.08f * sphereAlpha),
                0.25f to Color.White.copy(alpha = 0.03f * sphereAlpha),
                0.5f to Color.Transparent,
                0.75f to Color.White.copy(alpha = 0.03f * sphereAlpha),
                1f to Color(0xFF5EB0EF).copy(alpha = 0.08f * sphereAlpha),
            ),
            center = Offset(cx, cy),
        ),
        radius = r, center = Offset(cx, cy), style = Stroke(1.5f),
    )

    // Sweep light
    if (sweepProgress > 0f) {
        val startA = 210f; val endA = 330f
        val curA = startA + (endA - startA) * sweepProgress
        val rad = curA * PI.toFloat() / 180f
        val lx = cx + r * cos(rad); val ly = cy + r * sin(rad)

        val warmWhite = Color(0xFFFFF8F0); val warmGold = Color(0xFFFFE4B5)

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to warmWhite.copy(alpha = 0.40f * alpha),
                    0.12f to warmGold.copy(alpha = 0.20f * alpha),
                    0.4f to warmGold.copy(alpha = 0.05f * alpha),
                    1f to Color.Transparent,
                ),
                center = Offset(lx, ly), radius = r * 0.30f,
            ),
            radius = r * 0.30f, center = Offset(lx, ly),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color.White.copy(alpha = 0.75f * alpha),
                    0.1f to warmWhite.copy(alpha = 0.40f * alpha),
                    0.35f to warmWhite.copy(alpha = 0.08f * alpha),
                    1f to Color.Transparent,
                ),
                center = Offset(lx, ly), radius = r * 0.10f,
            ),
            radius = r * 0.10f, center = Offset(lx, ly),
        )

        // Trail
        for (t in 1..10) {
            val tp = (sweepProgress - 0.12f * t / 10f).coerceAtLeast(0f)
            val ta = startA + (endA - startA) * tp
            val tr = ta * PI.toFloat() / 180f
            val trailAlpha = (1f - t / 10f) * 0.10f * alpha
            drawCircle(
                warmGold.copy(alpha = trailAlpha),
                r * 0.06f,
                Offset(cx + r * cos(tr), cy + r * sin(tr)),
            )
        }

        // Arc illumination
        for (deg in -20..20) {
            val a = curA + deg
            val ar = a * PI.toFloat() / 180f
            val intensity = (1f - (kotlin.math.abs(deg) / 20f)) * 0.20f * alpha
            drawCircle(warmWhite.copy(alpha = intensity), 2.5f, Offset(cx + r * cos(ar), cy + r * sin(ar)))
        }
    }
}
