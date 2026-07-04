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
import lux.obris.app.core.theme.BrandCyan
import lux.obris.app.core.theme.BrandOrange
import lux.obris.app.core.theme.BrandOrangeLight
import lux.obris.app.core.theme.BrandPurple
import kotlin.random.Random

/**
 * Welcome screen — landscape layout.
 * Left: Logo + branding with energy particles.
 * Right: Auth buttons.
 */
@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    // ── Ambient animations ──
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    // ── Particles ──
    val particles = remember {
        List(50) {
            FloatArray(5).also { a ->
                a[0] = Random.nextFloat()  // x
                a[1] = Random.nextFloat()  // y
                a[2] = Random.nextFloat() * 2f + 0.5f  // size
                a[3] = Random.nextFloat()  // brightness
                a[4] = Random.nextFloat()  // offset
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0608)),
    ) {
        // ── Ambient background — energy glow + particles ──
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val w = size.width; val h = size.height

            // Orange energy glow (top-left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandOrange.copy(alpha = 0.10f * pulse), Color.Transparent),
                    center = Offset(w * 0.2f, h * 0.25f), radius = w * 0.30f,
                ),
                radius = w * 0.30f, center = Offset(w * 0.2f, h * 0.25f),
            )

            // Cyan energy glow (bottom-right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandCyan.copy(alpha = 0.06f * pulse), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.75f), radius = w * 0.25f,
                ),
                radius = w * 0.25f, center = Offset(w * 0.8f, h * 0.75f),
            )

            // Purple energy (center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPurple.copy(alpha = 0.05f * pulse), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.20f,
                ),
                radius = w * 0.20f, center = Offset(w * 0.5f, h * 0.5f),
            )

            // Particles — orange and cyan sparks
            particles.forEach { p ->
                val t = ((drift + p[4]) % 1f)
                val alpha = (p[3] * 0.3f + t * 0.4f * p[3]).coerceIn(0f, 0.6f)
                val color = if (p[3] > 0.5f) BrandOrange else BrandCyan
                drawCircle(color.copy(alpha = alpha), p[2], Offset(p[0] * w, p[1] * h))
            }
        }

        // ── Landscape layout ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── LEFT: Logo + tagline ──
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Logo
                ObrisLogo()

                Spacer(modifier = Modifier.height(14.dp))

                // Tagline
                Text(
                    text = "PLAY  •  COMPETE  •  WIN",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    color = BrandOrange.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }

            // ── RIGHT: Auth buttons ──
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Google button
                AuthButton(
                    iconRes = R.drawable.ic_google,
                    text = "Continue with Google",
                    onClick = onGoogleClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email button
                AuthButton(
                    iconRes = R.drawable.ic_email,
                    text = "Continue with Email",
                    onClick = onEmailClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Divider
                Row(Modifier.fillMaxWidth(0.8f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(0.5.dp).background(BrandOrange.copy(alpha = 0.12f)))
                    Text("or", Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A4A50))
                    Box(Modifier.weight(1f).height(0.5.dp).background(BrandOrange.copy(alpha = 0.12f)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guest
                Text(
                    text = "Play as Guest",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandOrange.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGuestClick)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Legal
                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2A1E24),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

/** Auth button — dark with orange/cyan gradient border */
@Composable
private fun AuthButton(iconRes: Int, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(50.dp)
            .clip(shape)
            .background(Color(0xFF120C10))
            .border(0.8.dp, Brush.linearGradient(listOf(BrandOrange.copy(alpha = 0.20f), BrandCyan.copy(alpha = 0.10f))), shape)
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), null, Modifier.size(20.dp), tint = Color.Unspecified)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFFD0C4C8))
    }
}
