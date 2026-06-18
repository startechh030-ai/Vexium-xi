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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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

    // Subtle breathing animation for the globe
    val infiniteTransition = rememberInfiniteTransition(label = "globe_breath")
    val breathe = infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF2F6FA)),
    ) {
        // ── Globe / Sphere Effect ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSphere(isDark = isDark, breatheScale = breathe.value)
        }

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.7f))

            // ── Logo (SVG) ──
            VexiumLogo(isDark = isDark)

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tagline ──
            Text(
                text = "Play.  Earn.  Trade.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                letterSpacing = 3.sp,
                color = if (isDark) Color(0xFF555555) else Color(0xFF8A9BB0),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1.3f))

            // ── Buttons ──
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

            // ── OR divider ──
            OrDivider(isDark = isDark)

            Spacer(modifier = Modifier.height(18.dp))

            // ── Guest ──
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

            // ── Legal ──
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
//  SOCIAL AUTH BUTTON
// ══════════════════════════════════════
@Composable
private fun SocialButton(
    iconRes: Int,
    text: String,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp) // more curved edges

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
                indication = ripple(
                    bounded = true,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f),
                ),
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
//  "OR" DIVIDER
// ══════════════════════════════════════
@Composable
private fun OrDivider(isDark: Boolean) {
    val lineColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFDDE2E8)
    val textColor = if (isDark) Color(0xFF444444) else Color(0xFFAAB0B8)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(lineColor),
        )
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(lineColor),
        )
    }
}

// ══════════════════════════════════════
//  3D SPHERE / GLOBE EFFECT
// ══════════════════════════════════════
private fun DrawScope.drawSphere(isDark: Boolean, breatheScale: Float) {
    val w = size.width
    val h = size.height

    // Sphere sits below center, between logo and buttons
    val sphereX = w / 2f
    val sphereY = h * 0.56f
    val sphereRadius = w * 0.60f * breatheScale

    if (isDark) {
        // ── Dark mode sphere: subtle luminous dome ──

        // Bottom half mask — the sphere rises from below
        // Large atmospheric haze
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFF1A1A1A).copy(alpha = 0.35f),
                    0.3f to Color(0xFF111111).copy(alpha = 0.20f),
                    0.6f to Color(0xFF0A0A0A).copy(alpha = 0.08f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY),
                radius = sphereRadius,
            ),
            radius = sphereRadius,
            center = Offset(sphereX, sphereY),
        )

        // Bright horizon arc — the "crescent" of light at the top of the sphere
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFFFFFFFF).copy(alpha = 0.09f),
                    0.4f to Color(0xFFDDDDDD).copy(alpha = 0.04f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY - sphereRadius * 0.55f),
                radius = sphereRadius * 0.45f,
            ),
            radius = sphereRadius * 0.45f,
            center = Offset(sphereX, sphereY - sphereRadius * 0.55f),
        )

        // Very tight bright highlight on the horizon line
        val horizonPath = Path().apply {
            arcTo(
                rect = Rect(
                    left = sphereX - sphereRadius * 0.8f,
                    top = sphereY - sphereRadius * 0.15f,
                    right = sphereX + sphereRadius * 0.8f,
                    bottom = sphereY + sphereRadius * 0.15f,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            close()
        }
        drawPath(
            path = horizonPath,
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.12f),
                    0.5f to Color.White.copy(alpha = 0.04f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY - sphereRadius * 0.05f),
                radius = sphereRadius * 0.5f,
            ),
        )

    } else {
        // ── Light mode sphere: icy blue atmospheric orb ──

        // Large blue atmosphere
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFFC4E2F4).copy(alpha = 0.55f),
                    0.35f to Color(0xFFD6ECF8).copy(alpha = 0.35f),
                    0.65f to Color(0xFFE6F2FA).copy(alpha = 0.15f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY),
                radius = sphereRadius * 1.1f,
            ),
            radius = sphereRadius * 1.1f,
            center = Offset(sphereX, sphereY),
        )

        // Inner bright ice core
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xFFB0DCF2).copy(alpha = 0.40f),
                    0.4f to Color(0xFFC8E8F8).copy(alpha = 0.20f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY - sphereRadius * 0.15f),
                radius = sphereRadius * 0.6f,
            ),
            radius = sphereRadius * 0.6f,
            center = Offset(sphereX, sphereY - sphereRadius * 0.15f),
        )

        // White crescent highlight at top
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.55f),
                    0.35f to Color.White.copy(alpha = 0.20f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY - sphereRadius * 0.52f),
                radius = sphereRadius * 0.28f,
            ),
            radius = sphereRadius * 0.28f,
            center = Offset(sphereX, sphereY - sphereRadius * 0.52f),
        )

        // Horizon glow line
        val horizonPath = Path().apply {
            arcTo(
                rect = Rect(
                    left = sphereX - sphereRadius * 0.75f,
                    top = sphereY - sphereRadius * 0.12f,
                    right = sphereX + sphereRadius * 0.75f,
                    bottom = sphereY + sphereRadius * 0.12f,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            close()
        }
        drawPath(
            path = horizonPath,
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.40f),
                    0.5f to Color(0xFFDCEEF8).copy(alpha = 0.15f),
                    1.0f to Color.Transparent,
                ),
                center = Offset(sphereX, sphereY - sphereRadius * 0.03f),
                radius = sphereRadius * 0.5f,
            ),
        )
    }
}
