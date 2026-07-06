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

// ── Colors ──
private val OrangeBright = Color(0xFFFF8C00)
private val OrangeLight = Color(0xFFFFAA33)
private val OrangeWarm = Color(0xFFFFF0E0)
private val CyanAccent = Color(0xFF00E5FF)

/**
 * Obris splash screen — landscape, pure Canvas.
 *
 * Visual: Half-sphere sits along the TOP edge of the screen.
 * A warm orange light sweeps left→right along the sphere arc.
 * Stars twinkle in the dark void below.
 * After the sweep, "OBRIS" letters assemble from scattered positions.
 *
 * Timeline:
 *  0.0-0.5s: Stars fade in
 *  0.5-1.5s: Half-sphere fades in at top
 *  1.5-3.5s: Warm light sweeps left→right along the arc
 *  3.5-5.0s: Letters of "OBRIS" fly together from edges
 *  5.0-6.0s: Hold
 *  6.0-6.8s: Fade out → navigate
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableIntStateOf(0) }

    // ── Animations ──
    val sphereAlpha = remember { Animatable(0f) }
    val sweepProgress = remember { Animatable(0f) }
    val screenFade = remember { Animatable(1f) }
    val loadingAlpha = remember { Animatable(0f) }

    // Text assembly: 0 = scattered, 1 = assembled
    var textTargetAssembly by remember { mutableFloatStateOf(0f) }
    val textAssembly by animateFloatAsState(
        targetValue = textTargetAssembly,
        animationSpec = tween(800),
        label = "textAssembly",
    )
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(
        targetValue = textTargetAlpha,
        animationSpec = tween(600),
        label = "textAlpha",
    )

    // Stars twinkle
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle",
    )

    // Stars data — generated once
    val stars = remember {
        List(50) { floatArrayOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 1.5f + 0.3f, Random.nextFloat(), Random.nextFloat()) }
    }

    // ── Timeline ──
    LaunchedEffect(Unit) {
        try {
            Log.d(TAG, "Splash start")
            delay(500)

            // Phase 1: Sphere fades in
            phase = 1
            sphereAlpha.animateTo(1f, tween(1000))

            // Phase 2: Light sweeps left → right
            phase = 2
            sweepProgress.animateTo(1f, tween(2000, easing = LinearEasing))
            delay(200)

            // Phase 3: Text assembles
            phase = 3
            textTargetAlpha = 1f
            textTargetAssembly = 1f
            loadingAlpha.animateTo(0.5f, tween(400))
            delay(1200)

            // Phase 4: Hold
            phase = 4
            delay(600)

            // Phase 5: Fade out
            phase = 5
            screenFade.animateTo(0f, tween(800))

            Log.d(TAG, "Splash done")
            onSplashFinished()
        } catch (e: Exception) {
            Log.e(TAG, "Splash error: ${e.message}", e)
            onSplashFinished()
        }
    }

    // ── UI ──
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

            // ── Stars ──
            drawStars(stars, twinkle, fo, w, h)

            // ── Half-sphere at TOP edge ──
            if (sphereAlpha.value > 0f) {
                drawTopHalfSphere(w, h, sphereAlpha.value * fo)
            }

            // ── Warm light sweep along top arc ──
            if (sweepProgress.value > 0f && phase >= 2) {
                drawArcSweep(w, h, sweepProgress.value, fo)
            }
        }

        // ── "OBRIS" text — letters assemble from scattered positions ──
        if (textAlpha > 0f) {
            // Each letter scatters from a different direction then converges to center
            val letters = "OBRIS"
            val scatterOffsets = remember {
                listOf(
                    Pair(-180f, -60f),   // O from top-left
                    Pair(-90f, 80f),     // B from bottom-left
                    Pair(0f, -100f),     // R from top
                    Pair(90f, 80f),      // I from bottom-right
                    Pair(180f, -60f),    // S from top-right
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                letters.forEachIndexed { i, char ->
                    // Scatter offset reduces as assembly approaches 1
                    val scatter = 1f - textAssembly
                    val ox = scatterOffsets[i].first * scatter
                    val oy = scatterOffsets[i].second * scatter
                    // Space letters horizontally: centered, each 36dp apart
                    val letterSpacing = (i - 2) * 36

                    Text(
                        text = char.toString(),
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = if (i % 2 == 0) {
                                OrangeBright.copy(alpha = textAlpha * screenFade.value)
                            } else {
                                Color.White.copy(alpha = textAlpha * screenFade.value)
                            },
                        ),
                        modifier = Modifier.offset(
                            x = (letterSpacing + ox.toInt()).dp,
                            y = oy.toInt().dp,
                        ),
                    )
                }
            }
        }

        // ── Loading text ──
        if (loadingAlpha.value > 0f) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = "Loading...",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = OrangeBright.copy(alpha = loadingAlpha.value * screenFade.value),
                        letterSpacing = 3.sp,
                    ),
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// DRAWING FUNCTIONS
// ═══════════════════════════════════════

/** Draw twinkling stars scattered across the screen */
private fun DrawScope.drawStars(
    stars: List<FloatArray>, twinkle: Float, alpha: Float,
    w: Float, h: Float,
) {
    stars.forEach { s ->
        val t = ((twinkle + s[4]) % 1f)
        val starAlpha = (s[3] * 0.3f + t * 0.5f * s[3]).coerceIn(0f, 0.6f) * alpha
        if (starAlpha > 0.01f) {
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = s[2].coerceAtLeast(0.5f),
                center = Offset(s[0] * w, s[1] * h),
            )
        }
    }
}

/**
 * Draw a half-sphere sitting at the TOP edge.
 * The sphere center is ABOVE the screen — only the bottom arc is visible.
 * This creates a horizon effect at the top.
 */
private fun DrawScope.drawTopHalfSphere(w: Float, h: Float, alpha: Float) {
    // Sphere center is above the screen
    val r = w * 0.8f
    val cx = w / 2f
    val cy = -r * 0.42f  // Center above screen — bottom arc visible at top

    // Sphere body — very dark, barely lighter than black
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFF030306).copy(alpha = alpha),
                0.90f to Color(0xFF020204).copy(alpha = alpha),
                0.97f to Color(0xFF080810).copy(alpha = alpha),
                1f to Color(0xFF0E0E18).copy(alpha = alpha),
            ),
            center = Offset(cx, cy),
            radius = r.coerceAtLeast(1f),
        ),
        radius = r,
        center = Offset(cx, cy),
    )

    // Edge glow rings
    for (i in 1..4) {
        val sw = (5 - i) * 3f
        val a = (0.05f - i * 0.01f).coerceAtLeast(0.005f) * alpha
        drawCircle(
            color = OrangeBright.copy(alpha = a),
            radius = r + sw,
            center = Offset(cx, cy),
            style = Stroke(width = sw),
        )
    }

    // Sharp edge line with orange tint
    drawCircle(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to OrangeBright.copy(alpha = 0.12f * alpha),
                0.15f to OrangeBright.copy(alpha = 0.05f * alpha),
                0.35f to Color.Transparent,
                0.5f to Color.Transparent,
                0.65f to Color.Transparent,
                0.85f to OrangeBright.copy(alpha = 0.05f * alpha),
                1f to OrangeBright.copy(alpha = 0.12f * alpha),
            ),
            center = Offset(cx, cy),
        ),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f),
    )
}

/** Draw the warm orange light sweeping left→right along the top arc */
private fun DrawScope.drawArcSweep(w: Float, h: Float, progress: Float, alpha: Float) {
    val r = w * 0.8f
    val cx = w / 2f
    val cy = -r * 0.42f

    // Light travels along the bottom arc of the sphere (visible part)
    // Angles: from ~150° (left) to ~30° (right) — bottom arc
    val startAngle = 150f
    val endAngle = 30f
    val currentAngle = startAngle - (startAngle - endAngle) * progress
    val rad = currentAngle * PI.toFloat() / 180f

    val lx = cx + r * cos(rad)
    val ly = cy + r * sin(rad)

    // Wide soft glow
    val glowR = (r * 0.25f).coerceAtLeast(10f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to OrangeWarm.copy(alpha = 0.30f * alpha),
                0.15f to OrangeLight.copy(alpha = 0.15f * alpha),
                0.4f to OrangeBright.copy(alpha = 0.04f * alpha),
                1f to Color.Transparent,
            ),
            center = Offset(lx, ly),
            radius = glowR,
        ),
        radius = glowR,
        center = Offset(lx, ly),
    )

    // Bright core
    val coreR = (r * 0.08f).coerceAtLeast(5f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = 0.50f * alpha),
                0.15f to OrangeWarm.copy(alpha = 0.30f * alpha),
                0.4f to OrangeWarm.copy(alpha = 0.06f * alpha),
                1f to Color.Transparent,
            ),
            center = Offset(lx, ly),
            radius = coreR,
        ),
        radius = coreR,
        center = Offset(lx, ly),
    )

    // Trail behind the light
    for (t in 1..8) {
        val tp = (progress - 0.08f * t / 8f).coerceAtLeast(0f)
        val ta = startAngle - (startAngle - endAngle) * tp
        val tr = ta * PI.toFloat() / 180f
        val trailAlpha = (1f - t / 8f) * 0.06f * alpha
        val trailR = (r * 0.04f).coerceAtLeast(3f)
        if (trailAlpha > 0.001f) {
            drawCircle(
                OrangeLight.copy(alpha = trailAlpha),
                trailR,
                Offset(cx + r * cos(tr), cy + r * sin(tr)),
            )
        }
    }

    // Illuminate the arc near the light
    for (deg in -12..12) {
        val a = currentAngle + deg
        val ar = a * PI.toFloat() / 180f
        val intensity = (1f - (abs(deg) / 12f)) * 0.10f * alpha
        if (intensity > 0.001f) {
            drawCircle(
                OrangeWarm.copy(alpha = intensity),
                1.5f,
                Offset(cx + r * cos(ar), cy + r * sin(ar)),
            )
        }
    }
}
