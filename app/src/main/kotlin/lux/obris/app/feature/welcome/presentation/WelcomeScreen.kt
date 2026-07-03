package lux.obris.app.feature.welcome.presentation

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.obris.app.R
import lux.obris.app.core.components.ObrisLogo
import lux.obris.app.core.theme.NeonCyan
import lux.obris.app.core.theme.NeonPurple
import lux.obris.app.core.theme.NeonPink
import lux.obris.app.core.theme.SpaceBlue
import kotlin.random.Random

/**
 * Welcome / Auth screen — landscape layout.
 * Left side: Logo + branding with space background.
 * Right side: Auth buttons.
 */
@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "space")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    // Stars — generated once
    val stars = remember { List(80) { StarPoint(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 0.5f, Random.nextFloat(), Random.nextFloat()) } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // ── Space background with stars + nebula ──
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val w = size.width; val h = size.height

            // Nebula glow — purple/cyan
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.08f * pulse), Color.Transparent),
                    center = Offset(w * 0.25f, h * 0.3f), radius = w * 0.35f,
                ),
                radius = w * 0.35f, center = Offset(w * 0.25f, h * 0.3f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.06f * pulse), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.7f), radius = w * 0.25f,
                ),
                radius = w * 0.25f, center = Offset(w * 0.15f, h * 0.7f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPink.copy(alpha = 0.04f * pulse), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.5f), radius = w * 0.2f,
                ),
                radius = w * 0.2f, center = Offset(w * 0.8f, h * 0.5f),
            )

            // Stars
            stars.forEach { s ->
                val t = ((drift + s.offset) % 1f)
                val alpha = (s.brightness * 0.3f + t * 0.5f * s.brightness).coerceIn(0f, 0.7f)
                drawCircle(Color.White.copy(alpha = alpha), s.size, Offset(s.x * w, s.y * h))
                // Shooting trails for bright stars
                if (s.brightness > 0.75f && t > 0.6f) {
                    drawLine(
                        Color.White.copy(alpha = alpha * 0.2f),
                        Offset(s.x * w, s.y * h),
                        Offset(s.x * w - s.size * 12f, s.y * h + s.size * 4f),
                        strokeWidth = 0.6f,
                    )
                }
            }
        }

        // ── Landscape layout: Left = branding, Right = auth ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── LEFT: Logo + tagline ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ObrisLogo()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PLAY  •  COMPETE  •  WIN",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp,
                    color = NeonCyan.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }

            // ── RIGHT: Auth buttons ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Google
                AuthButton(
                    iconRes = R.drawable.ic_google,
                    text = "Continue with Google",
                    onClick = onGoogleClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                AuthButton(
                    iconRes = R.drawable.ic_email,
                    text = "Continue with Email",
                    onClick = onEmailClick,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Row(Modifier.fillMaxWidth(0.8f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(0.5.dp).background(Color(0xFF1A1A30)))
                    Text("or", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF3A3A55))
                    Box(Modifier.weight(1f).height(0.5.dp).background(Color(0xFF1A1A30)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guest
                Text(
                    text = "Try as Guest",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGuestClick)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legal
                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF222240),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

// ── Star data ──
private data class StarPoint(val x: Float, val y: Float, val size: Float, val brightness: Float, val offset: Float)

// ── Auth button — cyberpunk style ──
@Composable
private fun AuthButton(iconRes: Int, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(50.dp)
            .clip(shape)
            .background(Color(0xFF0A0A16))
            .border(0.8.dp, Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.15f), NeonPurple.copy(alpha = 0.08f))), shape)
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), null, Modifier.size(20.dp), tint = Color.Unspecified)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFFCCCCDD))
    }
}
