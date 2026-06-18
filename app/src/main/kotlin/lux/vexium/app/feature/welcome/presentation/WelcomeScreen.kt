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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Subtle ambient breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF4F7FA)),
    ) {
        // ── Ambient background glow ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isDark) {
                // Dark: very subtle blue ambient glow top-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A3A5C).copy(alpha = 0.12f * ambientPulse),
                            Color(0xFF0D1F33).copy(alpha = 0.06f * ambientPulse),
                            Color.Transparent,
                        ),
                        center = Offset(w / 2f, h * 0.28f),
                        radius = w * 0.7f,
                    ),
                    radius = w * 0.7f,
                    center = Offset(w / 2f, h * 0.28f),
                )
                // Subtle warm bottom glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A).copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                        center = Offset(w / 2f, h * 0.75f),
                        radius = w * 0.5f,
                    ),
                    radius = w * 0.5f,
                    center = Offset(w / 2f, h * 0.75f),
                )
            } else {
                // Light: soft icy blue orb behind logo area
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD6ECFA).copy(alpha = 0.70f * ambientPulse),
                            Color(0xFFE4F2FC).copy(alpha = 0.35f * ambientPulse),
                            Color.Transparent,
                        ),
                        center = Offset(w / 2f, h * 0.32f),
                        radius = w * 0.55f,
                    ),
                    radius = w * 0.55f,
                    center = Offset(w / 2f, h * 0.32f),
                )
                // Subtle warm bottom glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEFF4F8).copy(alpha = 0.50f),
                            Color.Transparent,
                        ),
                        center = Offset(w / 2f, h * 0.72f),
                        radius = w * 0.45f,
                    ),
                    radius = w * 0.45f,
                    center = Offset(w / 2f, h * 0.72f),
                )
            }
        }

        // ── Main Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.8f))

            // ── Logo ──
            VexiumLogo(isDark = isDark)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tagline ──
            Text(
                text = "Play. Earn. Trade.",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF6B7C8E),
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1.2f))

            // ── Auth Buttons ──
            AuthButton(
                iconRes = R.drawable.ic_google,
                text = "Continue with Google",
                isDark = isDark,
                onClick = onGoogleClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                iconRes = R.drawable.ic_telegram,
                text = "Continue with Telegram",
                isDark = isDark,
                onClick = onTelegramClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthButton(
                iconRes = R.drawable.ic_email,
                text = "Continue with Email",
                isDark = isDark,
                onClick = onEmailClick,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider with "or" ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(0.5.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f)
                            else Color.Black.copy(alpha = 0.08f)
                        ),
                )
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFF999999),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(0.5.dp)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f)
                            else Color.Black.copy(alpha = 0.08f)
                        ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Guest Button ──
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onGuestClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Try as Guest",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF2A6FAC),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Footer ──
            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ══════════════════════════════════════
//  AUTH BUTTON
// ══════════════════════════════════════
@Composable
private fun AuthButton(
    iconRes: Int,
    text: String,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .then(
                if (isDark) {
                    Modifier
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE4E8EC), shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified, // preserve original icon colors
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1A1A1A),
        )
    }
}
