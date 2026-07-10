package lux.obris.app.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val IceBlue = Color(0xFF7DD3FC)
private val CyanGlow = Color(0xFF38BDF8)

/**
 * Gamified loading overlay — hexagonal spinner with orbiting particles.
 * Not a simple rotation — has multiple layers moving at different speeds.
 * Use for in-app module loading.
 */
@Composable
fun GamefyLoading(
    message: String = "Loading...",
) {
    val inf = rememberInfiniteTransition(label = "gamefy")

    // Outer hex rotation
    val outerRot by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "outer",
    )
    // Inner hex — counter-rotate
    val innerRot by inf.animateFloat(
        360f, 0f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "inner",
    )
    // Orbiting particles
    val particleAngle by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "particles",
    )
    // Pulse
    val pulse by inf.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.80f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(64.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Outer hexagon
                rotate(outerRot, Offset(cx, cy)) {
                    drawHexagon(cx, cy, size.width * 0.42f, IceBlue.copy(alpha = 0.4f * pulse), 2f)
                }

                // Inner hexagon — counter-rotating
                rotate(innerRot, Offset(cx, cy)) {
                    drawHexagon(cx, cy, size.width * 0.25f, CyanGlow.copy(alpha = 0.7f * pulse), 1.5f)
                }

                // Center dot
                drawCircle(Color.White.copy(alpha = pulse), 3f, Offset(cx, cy))

                // 3 orbiting particles at different speeds
                for (i in 0..2) {
                    val angle = (particleAngle + i * 120f) * PI.toFloat() / 180f
                    val orbitR = size.width * 0.35f
                    val px = cx + orbitR * cos(angle)
                    val py = cy + orbitR * sin(angle)
                    drawCircle(IceBlue.copy(alpha = 0.8f * pulse), 2.5f, Offset(px, py))
                    // Trail
                    val trailAngle = ((particleAngle + i * 120f) - 20f) * PI.toFloat() / 180f
                    drawCircle(IceBlue.copy(alpha = 0.3f * pulse), 1.5f, Offset(cx + orbitR * cos(trailAngle), cy + orbitR * sin(trailAngle)))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = message.uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = IceBlue.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }
    }
}

/** Draw a hexagon outline */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(
    cx: Float, cy: Float, radius: Float, color: Color, strokeWidth: Float,
) {
    val path = Path().apply {
        for (i in 0..5) {
            val angle = (60f * i - 30f) * PI.toFloat() / 180f
            val x = cx + radius * cos(angle)
            val y = cy + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}
