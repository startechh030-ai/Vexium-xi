package lux.vexium.app.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen loading overlay — covers entire screen.
 * Used during auth transitions (sign-in, session checks, etc.)
 */
@Composable
fun FullScreenLoading(
    message: String = "Loading...",
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val bgColor = if (isDark) Color.Black else Color(0xFFF2F6FA)
    val accentColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC)
    val textColor = if (isDark) Color(0xFF666666) else Color(0xFF8A9BB0)

    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated spinner
            Canvas(modifier = Modifier.size(48.dp)) {
                val radius = size.minDimension / 2f - 4f
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Background ring
                drawCircle(
                    color = accentColor.copy(alpha = 0.12f),
                    radius = radius,
                    style = Stroke(width = 3.5f),
                )

                // Spinning arc
                val startAngle = rotation
                val sweepAngle = 100f
                drawArc(
                    color = accentColor.copy(alpha = pulse),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round),
                )

                // Glowing dot at the leading edge
                val dotAngle = (startAngle + sweepAngle) * PI.toFloat() / 180f
                val dotX = cx + radius * cos(dotAngle)
                val dotY = cy + radius * sin(dotAngle)
                drawCircle(
                    color = accentColor,
                    radius = 4f,
                    center = Offset(dotX, dotY),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }
    }
}
