package lux.vexium.app.feature.welcome.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.vexium.app.R
import lux.vexium.app.core.components.VexiumLogo

@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onTelegramClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF2F6FA)),
    ) {
        // ── Half-sphere behind buttons ──
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            drawHalfSphere(isDark = isDark, pulse = pulse)
        }

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.65f))

            VexiumLogo(isDark = isDark)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Play.  Earn.  Trade.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                letterSpacing = 3.sp,
                color = if (isDark) Color(0xFF555555) else Color(0xFF8A9BB0),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1.3f))

            // ── Auth Buttons ──
            SocialButton(
                iconRes = R.drawable.ic_google,
                text = "Continue with Google",
                isDark = isDark,
                onClick = onGoogleClick,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SocialButton(
                iconRes = R.drawable.ic_telegram,
                text = "Continue with Telegram",
                isDark = isDark,
                onClick = onTelegramClick,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SocialButton(
                iconRes = R.drawable.ic_email,
                text = "Continue with Email",
                isDark = isDark,
                onClick = onEmailClick,
            )

            Spacer(modifier = Modifier.height(22.dp))

            OrDivider(isDark = isDark)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Try as Guest",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onGuestClick,
                    )
                    .padding(horizontal = 28.dp, vertical = 10.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color(0xFF333333) else Color(0xFFB0B8C0),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ══════════════════════════════════════
//  HALF-SPHERE WITH GLOWING EDGE
// ══════════════════════════════════════
private fun DrawScope.drawHalfSphere(isDark: Boolean, pulse: Float) {
    val w = size.width
    val h = size.height

    // Sphere is very large — only the top half (upper arc) is visible.
    // It sits just above the button area. The center of the full sphere
    // is placed below the visible area, so we only see the curved top edge.
    val sphereRadius = w * 1.1f
    val sphereCenterX = w / 2f
    // Position: the top of the sphere arc should appear around 55-58% of screen height
    val sphereCenterY = h * 0.58f + sphereRadius

    if (isDark) {
        // ── Dark: Subtle glow on the sphere edge ──

        // Atmospheric haze behind the sphere top
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.88f to Color.Transparent,
                    0.94f to Color(0xFF222222).copy(alpha = 0.15f * pulse),
                    0.97f to Color(0xFF444444).copy(alpha = 0.10f * pulse),
                    1.0f to Color(0xFF666666).copy(alpha = 0.04f * pulse),
                ),
                center = Offset(sphereCenterX, sphereCenterY),
                radius = sphereRadius,
            ),
            radius = sphereRadius,
            center = Offset(sphereCenterX, sphereCenterY),
        )

        // Bright edge ring (the crescent glow)
        // Outer glow (wide, soft)
        drawCircle(
            color = Color.Transparent,
            radius = sphereRadius,
            center = Offset(sphereCenterX, sphereCenterY),
        )

        for (i in 1..5) {
            val strokeW = (6 - i) * 4f
            val alpha = (0.06f - i * 0.01f).coerceAtLeast(0.005f) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.95f to Color.White.copy(alpha = alpha),
                        1.0f to Color.White.copy(alpha = alpha * 0.3f),
                    ),
                    center = Offset(sphereCenterX, sphereCenterY),
                    radius = sphereRadius + strokeW,
                ),
                radius = sphereRadius + strokeW / 2f,
                center = Offset(sphereCenterX, sphereCenterY),
                style = Stroke(width = strokeW),
            )
        }

        // Sharp bright edge line
        drawCircle(
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.20f * pulse),
                    0.15f to Color.White.copy(alpha = 0.08f * pulse),
                    0.35f to Color.White.copy(alpha = 0.02f * pulse),
                    0.50f to Color.Transparent,
                    0.65f to Color.White.copy(alpha = 0.02f * pulse),
                    0.85f to Color.White.copy(alpha = 0.08f * pulse),
                    1.0f to Color.White.copy(alpha = 0.20f * pulse),
                ),
                center = Offset(sphereCenterX, sphereCenterY),
            ),
            radius = sphereRadius,
            center = Offset(sphereCenterX, sphereCenterY),
            style = Stroke(width = 1.5f),
        )

        // Fill the sphere body (dark, slightly lighter than pure black)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF060606),
                    0.85f to Color(0xFF040404),
                    0.96f to Color(0xFF080808),
                    1.0f to Color(0xFF0C0C0C),
                ),
                center = Offset(sphereCenterX, sphereCenterY),
                radius = sphereRadius,
            ),
            radius = sphereRadius - 1f,
            center = Offset(sphereCenterX, sphereCenterY),
        )

    } else {
        // ── Light: Icy blue sphere with soft glow edge ──

        // Atmospheric glow around the edge
        for (i in 1..4) {
            val strokeW = (5 - i) * 6f
            val alpha = (0.10f - i * 0.02f).coerceAtLeast(0.01f) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.94f to Color(0xFF8AC8E8).copy(alpha = alpha),
                        1.0f to Color(0xFFB0DCF2).copy(alpha = alpha * 0.4f),
                    ),
                    center = Offset(sphereCenterX, sphereCenterY),
                    radius = sphereRadius + strokeW,
                ),
                radius = sphereRadius + strokeW / 2f,
                center = Offset(sphereCenterX, sphereCenterY),
                style = Stroke(width = strokeW),
            )
        }

        // Sharp edge line
        drawCircle(
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF6AB4DC).copy(alpha = 0.30f * pulse),
                    0.15f to Color(0xFF90CCE8).copy(alpha = 0.15f * pulse),
                    0.35f to Color(0xFFB0DCF2).copy(alpha = 0.05f * pulse),
                    0.50f to Color.Transparent,
                    0.65f to Color(0xFFB0DCF2).copy(alpha = 0.05f * pulse),
                    0.85f to Color(0xFF90CCE8).copy(alpha = 0.15f * pulse),
                    1.0f to Color(0xFF6AB4DC).copy(alpha = 0.30f * pulse),
                ),
                center = Offset(sphereCenterX, sphereCenterY),
            ),
            radius = sphereRadius,
            center = Offset(sphereCenterX, sphereCenterY),
            style = Stroke(width = 1.5f),
        )

        // Fill sphere body (almost background color but slightly cooler)
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFFEFF4F8),
                    0.80f to Color(0xFFEAF0F5),
                    0.95f to Color(0xFFE4ECF2),
                    1.0f to Color(0xFFDDE6EE),
                ),
                center = Offset(sphereCenterX, sphereCenterY),
                radius = sphereRadius,
            ),
            radius = sphereRadius - 1f,
            center = Offset(sphereCenterX, sphereCenterY),
        )
    }
}

// ══════════════════════════════════════
//  SOCIAL BUTTON
// ══════════════════════════════════════
@Composable
private fun SocialButton(
    iconRes: Int,
    text: String,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .then(
                if (isDark) {
                    Modifier
                        .background(Color(0xFF0F0F0F))
                        .border(0.8.dp, Color(0xFF1E1E1E), shape)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, Color(0xFFDDE2E8), shape)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF222222),
        )
    }
}

// ══════════════════════════════════════
//  DIVIDER
// ══════════════════════════════════════
@Composable
private fun OrDivider(isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFDDE2E8)),
        )
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color(0xFF444444) else Color(0xFFAAB0B8),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFDDE2E8)),
        )
    }
}
