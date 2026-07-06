package lux.obris.app.feature.splash.presentation

import android.util.Log
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "ObrisSplash"

// ── Brand colors ──
private val OrangeBright = Color(0xFFFF8C00)
private val OrangeLight = Color(0xFFFFAA33)
private val OrangeWarm = Color(0xFFFFF0E0)
private val OrangeDim = Color(0xFF663300)

/**
 * Obris splash — landscape, pure Canvas.
 *
 * Half-sphere at BOTTOM edge (facing up like Vexium).
 * Orange light sweeps left→right along the arc.
 * Stars twinkle. "OBRIS" letters assemble.
 * Total: ~5 seconds.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableIntStateOf(0) }

    val sphereAlpha = remember { Animatable(0f) }
    val sweepProgress = remember { Animatable(0f) }
    val screenFade = remember { Animatable(1f) }
    val loadingAlpha = remember { Animatable(0f) }

    // Text assembly
    var textTargetAssembly by remember { mutableFloatStateOf(0f) }
    val textAssembly by animateFloatAsState(
        textTargetAssembly, tween(700), label = "assemble",
    )
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(
        textTargetAlpha, tween(500), label = "textAlpha",
    )

    // Stars
    val inf = rememberInfiniteTransition(label = "s")
    val twinkle by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "tw",
    )
    val stars = remember {
        List(55) {
            floatArrayOf(
                Random.nextFloat(), Random.nextFloat(),
                Random.nextFloat() * 1.5f + 0.3f,
                Random.nextFloat(), Random.nextFloat(),
            )
        }
    }

    // ── Timeline: total ~5 seconds ──
    LaunchedEffect(Unit) {
        try {
            delay(300)

            // Sphere fades in (0.3-1.0s)
            phase = 1
            sphereAlpha.animateTo(1f, tween(700))

            // Light sweeps (1.0-2.8s)
            phase = 2
            sweepProgress.animateTo(1f, tween(1800, easing = LinearEasing))

            // Text assembles (2.8-3.8s)
            phase = 3
            textTargetAlpha = 1f
            textTargetAssembly = 1f
            loadingAlpha.animateTo(0.5f, tween(300))
            delay(800)

            // Hold (3.8-4.3s)
            phase = 4
            delay(500)

            // Fade out (4.3-5.0s)
            phase = 5
            screenFade.animateTo(0f, tween(700))
            onSplashFinished()
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            val fo = screenFade.value

            // Stars
            drawStars(stars, twinkle, fo, w, h)

            // Half-sphere at BOTTOM — facing up
            if (sphereAlpha.value > 0f) {
                drawBottomSphere(w, h, sphereAlpha.value * fo)
            }

            // Light sweep along bottom arc
            if (sweepProgress.value > 0f && phase >= 2) {
                drawBottomArcSweep(w, h, sweepProgress.value, fo)
            }
        }

        // ── "OBRIS" letters assemble ──
        if (textAlpha > 0f) {
            val scatterDirs = remember {
                listOf(
                    Pair(-160f, -50f),
                    Pair(-80f, 60f),
                    Pair(0f, -80f),
                    Pair(80f, 60f),
                    Pair(160f, -50f),
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                "OBRIS".forEachIndexed { i, c ->
                    val scatter = 1f - textAssembly
                    val ox = scatterDirs[i].first * scatter
                    val oy = scatterDirs[i].second * scatter
                    val spacing = (i - 2) * 36

                    Text(
                        text = c.toString(),
                        style = TextStyle(
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = if (i % 2 == 0) {
                                OrangeBright.copy(alpha = textAlpha * screenFade.value)
                            } else {
                                Color.White.copy(alpha = textAlpha * screenFade.value)
                            },
                        ),
                        modifier = Modifier.offset(
                            x = (spacing + ox.toInt()).dp,
                            y = (oy.toInt() - 20).dp, // Slightly above center
                        ),
                    )
                }
            }
        }

        // Loading
        if (loadingAlpha.value > 0f) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = "Loading...",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = OrangeBright.copy(alpha = loadingAlpha.value * screenFade.value),
                        letterSpacing = 3.sp,
                    ),
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
        }
    }
}

// ══════════════════════════════════════
// DRAWING
// ══════════════════════════════════════

private fun DrawScope.drawStars(
    stars: List<FloatArray>, twinkle: Float, alpha: Float, w: Float, h: Float,
) {
    stars.forEach { s ->
        val t = ((twinkle + s[4]) % 1f)
        val a = (s[3] * 0.3f + t * 0.5f * s[3]).coerceIn(0f, 0.55f) * alpha
        if (a > 0.01f) {
            drawCircle(Color.White.copy(alpha = a), s[2].coerceAtLeast(0.5f), Offset(s[0] * w, s[1] * h))
        }
    }
}

/**
 * Half-sphere at BOTTOM of screen — arc curves UPWARD.
 * Sphere center is far below screen. Only the top arc peeks up.
 * Like a planet rising from the bottom edge.
 */
private fun DrawScope.drawBottomSphere(w: Float, h: Float, alpha: Float) {
    // Sphere is huge — center far below screen
    // Only the top arc is visible, curving UPWARD
    val r = w * 1.1f
    val cx = w / 2f
    val cy = h + r * 0.60f // Far below — top arc barely peeks above bottom edge

    // Ambient glow — makes the orb feel alive and detached
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.80f to Color.Transparent,
                0.90f to OrangeDim.copy(alpha = 0.06f * alpha),
                0.95f to OrangeBright.copy(alpha = 0.04f * alpha),
                1f to OrangeBright.copy(alpha = 0.02f * alpha),
            ),
            center = Offset(cx, cy),
            radius = (r * 1.15f).coerceAtLeast(10f),
        ),
        radius = (r * 1.15f).coerceAtLeast(10f),
        center = Offset(cx, cy),
    )

    // Sphere body
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFF030306).copy(alpha = alpha),
                0.88f to Color(0xFF020204).copy(alpha = alpha),
                0.95f to Color(0xFF0A0A14).copy(alpha = alpha),
                1f to Color(0xFF10101C).copy(alpha = alpha),
            ),
            center = Offset(cx, cy),
            radius = r.coerceAtLeast(1f),
        ),
        radius = r,
        center = Offset(cx, cy),
    )

    // Edge glow rings — orange tinted
    for (i in 1..5) {
        val sw = (6 - i) * 3.5f
        val a = (0.07f - i * 0.012f).coerceAtLeast(0.004f) * alpha
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.96f to OrangeBright.copy(alpha = a),
                    1f to OrangeBright.copy(alpha = a * 0.2f),
                ),
                center = Offset(cx, cy),
                radius = (r + sw + 5f).coerceAtLeast(10f),
            ),
            radius = r + sw / 2f,
            center = Offset(cx, cy),
            style = Stroke(width = sw),
        )
    }

    // Sharp edge arc — brighter at the sides where arc meets screen edges
    drawCircle(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to OrangeBright.copy(alpha = 0.18f * alpha),
                0.1f to OrangeLight.copy(alpha = 0.10f * alpha),
                0.25f to OrangeBright.copy(alpha = 0.04f * alpha),
                0.4f to Color.Transparent,
                0.5f to Color.Transparent,
                0.6f to Color.Transparent,
                0.75f to OrangeBright.copy(alpha = 0.04f * alpha),
                0.9f to OrangeLight.copy(alpha = 0.10f * alpha),
                1f to OrangeBright.copy(alpha = 0.18f * alpha),
            ),
            center = Offset(cx, cy),
        ),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = 1.8f),
    )
}

/** Warm orange light sweeps left→right along the upward-facing arc */
private fun DrawScope.drawBottomArcSweep(w: Float, h: Float, progress: Float, alpha: Float) {
    val r = w * 1.1f
    val cx = w / 2f
    val cy = h + r * 0.60f

    // Light travels along the TOP arc of the sphere (the visible crescent)
    // Angles: 210° (left) → 330° (right) on the circle
    val startAngle = 210f
    val endAngle = 330f
    val currentAngle = startAngle + (endAngle - startAngle) * progress
    val rad = currentAngle * PI.toFloat() / 180f

    val lx = cx + r * cos(rad)
    val ly = cy + r * sin(rad)

    // Wide warm glow
    val glowR = (r * 0.22f).coerceAtLeast(10f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to OrangeWarm.copy(alpha = 0.28f * alpha),
                0.12f to OrangeLight.copy(alpha = 0.14f * alpha),
                0.35f to OrangeBright.copy(alpha = 0.04f * alpha),
                1f to Color.Transparent,
            ),
            center = Offset(lx, ly),
            radius = glowR,
        ),
        radius = glowR,
        center = Offset(lx, ly),
    )

    // Bright core
    val coreR = (r * 0.07f).coerceAtLeast(4f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = 0.55f * alpha),
                0.12f to OrangeWarm.copy(alpha = 0.28f * alpha),
                0.4f to OrangeWarm.copy(alpha = 0.05f * alpha),
                1f to Color.Transparent,
            ),
            center = Offset(lx, ly),
            radius = coreR,
        ),
        radius = coreR,
        center = Offset(lx, ly),
    )

    // Trail
    for (t in 1..8) {
        val tp = (progress - 0.08f * t / 8f).coerceAtLeast(0f)
        val ta = startAngle + (endAngle - startAngle) * tp
        val tr = ta * PI.toFloat() / 180f
        val trailAlpha = (1f - t / 8f) * 0.05f * alpha
        val trailR = (r * 0.035f).coerceAtLeast(2f)
        if (trailAlpha > 0.001f) {
            drawCircle(
                OrangeLight.copy(alpha = trailAlpha),
                trailR,
                Offset(cx + r * cos(tr), cy + r * sin(tr)),
            )
        }
    }

    // Arc illumination near the light point
    for (deg in -10..10) {
        val a = currentAngle + deg
        val ar = a * PI.toFloat() / 180f
        val intensity = (1f - (abs(deg) / 10f)) * 0.08f * alpha
        if (intensity > 0.001f) {
            drawCircle(
                OrangeWarm.copy(alpha = intensity),
                1.5f,
                Offset(cx + r * cos(ar), cy + r * sin(ar)),
            )
        }
    }
}
