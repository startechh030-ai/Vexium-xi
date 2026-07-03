package lux.obris.app.feature.splash.presentation

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Colors ──
private val GlowBlue = Color(0xFF00A3FF)
private val GlowYellow = Color(0xFFFFD600)
private val DeepBlack = Color(0xFF050508)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    var phase by remember { mutableStateOf(0) }

    // ── Sweep angle rotation (the glow travels around the border) ──
    val infiniteTransition = rememberInfiniteTransition(label = "border_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep",
    )

    // ── Glow pulse ──
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // ── Text alpha ──
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(
        targetValue = textTargetAlpha,
        animationSpec = tween(durationMillis = 1200),
        label = "text_alpha",
    )

    // ── Text glow ──
    var textGlowTarget by remember { mutableFloatStateOf(0f) }
    val textGlow by animateFloatAsState(
        targetValue = textGlowTarget,
        animationSpec = tween(durationMillis = 800),
        label = "text_glow",
    )

    // ── Screen fade-out ──
    val screenAlpha = remember { Animatable(1f) }

    // ── Timeline ──
    LaunchedEffect(Unit) {
        // Phase 0: Border glow spinning (0-2s)
        delay(2000)

        // Phase 1: Text fades in translucent
        phase = 1
        textTargetAlpha = 0.35f
        delay(1500)

        // Phase 2: Text glows up
        phase = 2
        textTargetAlpha = 1f
        textGlowTarget = 1f
        delay(1000)

        // Phase 3: Fade out
        phase = 3
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(800),
        )

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: Animated edge glow border
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawEdgeGlow(
                sweepAngle = sweepAngle,
                pulseIntensity = glowPulse,
                alpha = screenAlpha.value,
            )
        }

        // Layer 2: Text
        if (phase >= 1) {
            val finalAlpha = textAlpha * screenAlpha.value

            // Glow behind text
            if (textGlow > 0f) {
                Text(
                    text = "Obris",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowBlue.copy(alpha = textGlow * 0.4f * screenAlpha.value),
                        letterSpacing = 6.sp,
                    ),
                )
            }

            // Main text
            Text(
                text = "Obris",
                style = TextStyle(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = finalAlpha),
                    letterSpacing = 6.sp,
                ),
            )
        }
    }
}

/**
 * Draws a glowing border that hugs the rounded-rectangle screen edges.
 * The glow color sweeps around the perimeter like a traveling light.
 */
private fun DrawScope.drawEdgeGlow(
    sweepAngle: Float,
    pulseIntensity: Float,
    alpha: Float,
) {
    val w = size.width
    val h = size.height
    val padding = 8f
    val cornerRadius = 36f
    val borderRect = Size(w - padding * 2, h - padding * 2)
    val borderOffset = Offset(padding, padding)
    val centerX = w / 2f
    val centerY = h / 2f

    // The "head" of the glow travels around the border.
    // We use a sweep gradient centered on the screen, with a bright
    // sector at `sweepAngle` that fades out in both directions.
    // This creates the look of a light beam chasing around the edges.

    val sweepRad = sweepAngle * PI.toFloat() / 180f

    // Build color stops: bright at sweepAngle, dark opposite
    fun buildSweepColors(baseColor: Color, intensity: Float): List<Color> {
        return listOf(
            baseColor.copy(alpha = 0.90f * intensity * alpha),
            baseColor.copy(alpha = 0.50f * intensity * alpha),
            Color.Transparent,
            Color.Transparent,
            Color.Transparent,
            baseColor.copy(alpha = 0.50f * intensity * alpha),
            baseColor.copy(alpha = 0.90f * intensity * alpha),
        )
    }

    // We'll draw two glow beams — one blue, one yellow — 180° apart
    val blueAngle = sweepAngle
    val yellowAngle = (sweepAngle + 180f) % 360f

    // ── Outer soft glow layers (3 passes for blur-like effect) ──
    for (i in 1..4) {
        val spread = 4f + i * 10f
        val glowAlpha = (0.20f - i * 0.04f).coerceAtLeast(0.02f) * pulseIntensity * alpha
        val expand = i * 6f

        // Blue beam
        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GlowBlue.copy(alpha = glowAlpha),
                    GlowBlue.copy(alpha = glowAlpha * 0.4f),
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    GlowBlue.copy(alpha = glowAlpha * 0.4f),
                    GlowBlue.copy(alpha = glowAlpha),
                ),
                center = Offset(centerX, centerY),
            ),
            topLeft = Offset(padding - expand, padding - expand),
            size = Size(borderRect.width + expand * 2, borderRect.height + expand * 2),
            cornerRadius = CornerRadius(cornerRadius + expand * 0.5f),
            style = Stroke(width = spread, cap = StrokeCap.Round),
        )

        // Yellow beam (opposite side)
        drawRoundRect(
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    ((yellowAngle - 40f).mod(360f) / 360f) to Color.Transparent,
                    ((yellowAngle - 10f).mod(360f) / 360f) to GlowYellow.copy(alpha = glowAlpha * 0.3f),
                    (yellowAngle.mod(360f) / 360f) to GlowYellow.copy(alpha = glowAlpha),
                    ((yellowAngle + 10f).mod(360f) / 360f) to GlowYellow.copy(alpha = glowAlpha * 0.3f),
                    ((yellowAngle + 40f).mod(360f) / 360f) to Color.Transparent,
                    1f to Color.Transparent,
                ),
                center = Offset(centerX, centerY),
            ),
            topLeft = Offset(padding - expand, padding - expand),
            size = Size(borderRect.width + expand * 2, borderRect.height + expand * 2),
            cornerRadius = CornerRadius(cornerRadius + expand * 0.5f),
            style = Stroke(width = spread, cap = StrokeCap.Round),
        )
    }

    // ── Main sharp border (thin, bright) ──
    // Blue side
    drawRoundRect(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                ((blueAngle - 50f).mod(360f) / 360f) to Color.Transparent,
                ((blueAngle - 15f).mod(360f) / 360f) to GlowBlue.copy(alpha = 0.5f * alpha),
                (blueAngle.mod(360f) / 360f) to GlowBlue.copy(alpha = 0.95f * alpha),
                ((blueAngle + 15f).mod(360f) / 360f) to GlowBlue.copy(alpha = 0.5f * alpha),
                ((blueAngle + 50f).mod(360f) / 360f) to Color.Transparent,
                1f to Color.Transparent,
            ),
            center = Offset(centerX, centerY),
        ),
        topLeft = borderOffset,
        size = borderRect,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 3f, cap = StrokeCap.Round),
    )

    // Yellow side
    drawRoundRect(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                ((yellowAngle - 50f).mod(360f) / 360f) to Color.Transparent,
                ((yellowAngle - 15f).mod(360f) / 360f) to GlowYellow.copy(alpha = 0.5f * alpha),
                (yellowAngle.mod(360f) / 360f) to GlowYellow.copy(alpha = 0.95f * alpha),
                ((yellowAngle + 15f).mod(360f) / 360f) to GlowYellow.copy(alpha = 0.5f * alpha),
                ((yellowAngle + 50f).mod(360f) / 360f) to Color.Transparent,
                1f to Color.Transparent,
            ),
            center = Offset(centerX, centerY),
        ),
        topLeft = borderOffset,
        size = borderRect,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 3f, cap = StrokeCap.Round),
    )

    // ── Subtle always-on dim border outline ──
    drawRoundRect(
        color = Color.White.copy(alpha = 0.04f * alpha),
        topLeft = borderOffset,
        size = borderRect,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 1.5f),
    )
}

/** Helper: Float mod that always returns positive */
private fun Float.mod(other: Float): Float {
    val result = this % other
    return if (result < 0) result + other else result
}
