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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Colors ──
private val GlowBlue = Color(0xFF00A3FF)
private val GlowYellow = Color(0xFFFFD600)
private val GlowBlack = Color(0xFF000000)
private val DeepBlack = Color(0xFF050508)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    // ── Phase control ──
    // Phase 0: Just border glow spinning (0 - 2s)
    // Phase 1: Text fades in translucent (2s - 3.5s)
    // Phase 2: Text glows up bright (3.5s - 4.5s)
    // Phase 3: Everything fades out → navigate (4.5s - 5.5s)
    var phase by remember { mutableStateOf(0) }
    var fadeOutAll by remember { mutableStateOf(false) }

    // ── Spinning border rotation (continuous, ~30fps feel) ──
    val infiniteTransition = rememberInfiniteTransition(label = "border_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    // ── Glow pulse (subtle breathing effect on the border) ──
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    // ── Text alpha: translucent fade-in ──
    var textTargetAlpha by remember { mutableFloatStateOf(0f) }
    val textAlpha by animateFloatAsState(
        targetValue = textTargetAlpha,
        animationSpec = tween(durationMillis = 1200),
        label = "text_alpha",
    )

    // ── Text glow intensity ──
    var textGlowTarget by remember { mutableFloatStateOf(0f) }
    val textGlow by animateFloatAsState(
        targetValue = textGlowTarget,
        animationSpec = tween(durationMillis = 800),
        label = "text_glow",
    )

    // ── Full screen fade-out ──
    val screenAlpha = remember { Animatable(1f) }

    // ── Phase timeline ──
    LaunchedEffect(Unit) {
        // Phase 0: Border spinning alone
        delay(2000)

        // Phase 1: Text fades in translucent
        phase = 1
        textTargetAlpha = 0.4f
        delay(1500)

        // Phase 2: Text glows up
        phase = 2
        textTargetAlpha = 1f
        textGlowTarget = 1f
        delay(1000)

        // Phase 3: Fade everything out
        phase = 3
        fadeOutAll = true
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(800),
        )

        // Navigate
        onSplashFinished()
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: Animated glow border
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val alpha = screenAlpha.value
            drawGlowBorder(
                rotation = rotation,
                glowIntensity = glowPulse,
                alpha = alpha,
            )
        }

        // Layer 2: "Vexium" text
        if (phase >= 1) {
            val finalAlpha = textAlpha * screenAlpha.value

            // Glow behind text
            if (textGlow > 0f) {
                Text(
                    text = "Vexium",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowBlue.copy(alpha = textGlow * 0.5f * screenAlpha.value),
                        letterSpacing = 6.sp,
                    ),
                )
            }

            // Main text
            Text(
                text = "Vexium",
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
 * Draws the spinning neon glow border around the screen edges.
 */
private fun DrawScope.drawGlowBorder(
    rotation: Float,
    glowIntensity: Float,
    alpha: Float,
) {
    val width = size.width
    val height = size.height
    val strokeWidth = 6f
    val padding = 12f
    val cornerRadius = 40f

    val borderSize = Size(width - padding * 2, height - padding * 2)
    val borderOffset = Offset(padding, padding)

    // ── Outer soft glow (wide, blurred look) ──
    for (i in 1..3) {
        val spread = strokeWidth + (i * 14f)
        val glowAlpha = (0.15f - i * 0.04f) * glowIntensity * alpha

        rotate(degrees = rotation) {
            drawRoundRect(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GlowBlue.copy(alpha = glowAlpha),
                        GlowYellow.copy(alpha = glowAlpha),
                        GlowBlack.copy(alpha = glowAlpha * 0.3f),
                        GlowBlue.copy(alpha = glowAlpha),
                    ),
                    center = Offset(width / 2f, height / 2f),
                ),
                topLeft = Offset(padding - i * 7f, padding - i * 7f),
                size = Size(
                    borderSize.width + i * 14f,
                    borderSize.height + i * 14f,
                ),
                cornerRadius = CornerRadius(cornerRadius + i * 4f),
                style = Stroke(width = spread, cap = StrokeCap.Round),
            )
        }
    }

    // ── Main sharp border ──
    rotate(degrees = rotation) {
        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GlowBlue.copy(alpha = 0.9f * alpha),
                    GlowYellow.copy(alpha = 0.9f * alpha),
                    GlowBlack.copy(alpha = 0.4f * alpha),
                    GlowBlue.copy(alpha = 0.9f * alpha),
                ),
                center = Offset(width / 2f, height / 2f),
            ),
            topLeft = borderOffset,
            size = borderSize,
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }

    // ── Inner subtle glow ──
    rotate(degrees = rotation) {
        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GlowBlue.copy(alpha = 0.08f * glowIntensity * alpha),
                    GlowYellow.copy(alpha = 0.05f * glowIntensity * alpha),
                    Color.Transparent,
                    GlowBlue.copy(alpha = 0.08f * glowIntensity * alpha),
                ),
                center = Offset(width / 2f, height / 2f),
            ),
            topLeft = Offset(padding + 8f, padding + 8f),
            size = Size(borderSize.width - 16f, borderSize.height - 16f),
            cornerRadius = CornerRadius(cornerRadius - 6f),
            style = Stroke(width = 3f),
        )
    }
}
