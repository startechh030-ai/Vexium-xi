package lux.obris.app.feature.auth.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AccountCreatedScreen(
    onContinue: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val accentColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC)

    // Animated checkmark
    val checkProgress = remember { Animatable(0f) }
    val circleProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        circleProgress.animateTo(1f, tween(600, easing = LinearEasing))
        checkProgress.animateTo(1f, tween(400, easing = LinearEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF2F6FA)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Animated check circle
            Canvas(modifier = Modifier.size(100.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f - 4f

                // Circle
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * circleProgress.value,
                    useCenter = false,
                    style = Stroke(width = 4f, cap = StrokeCap.Round),
                )

                // Checkmark
                if (checkProgress.value > 0f) {
                    val p = checkProgress.value
                    val startX = cx - r * 0.3f
                    val startY = cy + r * 0.05f
                    val midX = cx - r * 0.05f
                    val midY = cy + r * 0.3f
                    val endX = cx + r * 0.35f
                    val endY = cy - r * 0.2f

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(startX, startY)
                        if (p <= 0.5f) {
                            val t = p * 2f
                            lineTo(startX + (midX - startX) * t, startY + (midY - startY) * t)
                        } else {
                            lineTo(midX, midY)
                            val t = (p - 0.5f) * 2f
                            lineTo(midX + (endX - midX) * t, midY + (endY - midY) * t)
                        }
                    }
                    drawPath(path, accentColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Account Created!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111111),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Obris account is ready.\nStart playing, earning, and trading!",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDark) Color(0xFF666666) else Color(0xFF8A9BB0),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                ),
            ) {
                Text("Continue to Home", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
