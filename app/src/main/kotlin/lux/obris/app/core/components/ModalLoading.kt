package lux.obris.app.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Modal loading overlay — semi-transparent blur-like effect over current screen.
 * Blocks all interactions while loading.
 * Used for in-app operations (saving data, etc.)
 */
@Composable
fun ModalLoading(
    message: String = "Please wait...",
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val accentColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC)

    val infiniteTransition = rememberInfiniteTransition(label = "modal_loader")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )

    // Full-screen blocker — absorbs all taps
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) Color.Black.copy(alpha = 0.75f)
                else Color.White.copy(alpha = 0.80f),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* absorb taps */ },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Card
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isDark) Color(0xFF1A1A1A) else Color.White,
                )
                .padding(horizontal = 40.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    val radius = size.minDimension / 2f - 3f

                    drawCircle(
                        color = accentColor.copy(alpha = 0.10f),
                        radius = radius,
                        style = Stroke(width = 3f),
                    )

                    drawArc(
                        color = accentColor,
                        startAngle = rotation,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 3f, cap = StrokeCap.Round),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF999999) else Color(0xFF666666),
                )
            }
        }
    }
}
