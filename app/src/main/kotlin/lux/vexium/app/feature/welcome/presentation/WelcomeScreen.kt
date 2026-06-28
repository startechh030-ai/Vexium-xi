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
import kotlin.random.Random

@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onTelegramClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )

    // Particles
    val particles = remember {
        if (isDark) {
            // Stars for dark mode
            List(40) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 1.8f + 0.3f, Random.nextFloat(), Random.nextFloat()) }
        } else {
            // Ice particles for light mode
            List(30) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2.5f + 0.8f, Random.nextFloat(), Random.nextFloat()) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color(0xFFF0F5FA)),
    ) {
        // Background effects
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            if (isDark) {
                drawDarkBackground(pulse, drift, particles)
            } else {
                drawLightBackground(pulse, drift, particles)
            }
        }

        // Half-sphere
        Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            drawHalfSphere(isDark, pulse)
        }

        // Content
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
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
                color = if (isDark) Color(0xFF4A4A4A) else Color(0xFF7A90A8),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1.3f))

            SocialButton(R.drawable.ic_google, "Continue with Google", isDark, onGoogleClick)
            Spacer(modifier = Modifier.height(10.dp))
            SocialButton(R.drawable.ic_telegram, "Continue with Telegram", isDark, onTelegramClick)
            Spacer(modifier = Modifier.height(10.dp))
            SocialButton(R.drawable.ic_email, "Continue with Email", isDark, onEmailClick)

            Spacer(modifier = Modifier.height(22.dp))

            OrDivider(isDark)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Try as Guest",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGuestClick)
                    .padding(horizontal = 28.dp, vertical = 10.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color(0xFF2A2A2A) else Color(0xFFB0B8C0),
                textAlign = TextAlign.Center, lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

data class Particle(val x: Float, val y: Float, val size: Float, val brightness: Float, val offset: Float)

private fun DrawScope.drawDarkBackground(pulse: Float, drift: Float, particles: List<Particle>) {
    val w = size.width; val h = size.height

    // Deep blue ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0A1628).copy(alpha = 0.50f * pulse), Color(0xFF050D18).copy(alpha = 0.20f), Color.Transparent),
            center = Offset(w * 0.7f, h * 0.2f), radius = w * 0.6f,
        ),
        radius = w * 0.6f, center = Offset(w * 0.7f, h * 0.2f),
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF08122A).copy(alpha = 0.30f * pulse), Color.Transparent),
            center = Offset(w * 0.2f, h * 0.7f), radius = w * 0.4f,
        ),
        radius = w * 0.4f, center = Offset(w * 0.2f, h * 0.7f),
    )

    // Stars with shooting effect
    particles.forEach { p ->
        val t = ((drift + p.offset) % 1f)
        val starAlpha = (p.brightness * 0.4f + t * 0.6f * p.brightness) * 0.5f
        val shootX = p.x * w + (drift * 0.02f * w * (p.offset - 0.5f))
        drawCircle(Color.White.copy(alpha = starAlpha.coerceIn(0f, 0.8f)), p.size, Offset(shootX % w, p.y * h))

        // Shooting trail for bright stars
        if (p.brightness > 0.7f && t > 0.5f) {
            val trailLen = p.size * 8f
            drawLine(
                Color.White.copy(alpha = (starAlpha * 0.3f).coerceIn(0f, 0.3f)),
                start = Offset(shootX % w, p.y * h),
                end = Offset((shootX - trailLen) % w, p.y * h + trailLen * 0.3f),
                strokeWidth = 0.8f,
            )
        }
    }
}

private fun DrawScope.drawLightBackground(pulse: Float, drift: Float, particles: List<Particle>) {
    val w = size.width; val h = size.height

    // Ice blue ambient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFCCE4F6).copy(alpha = 0.45f * pulse), Color(0xFFDCEEF8).copy(alpha = 0.15f), Color.Transparent),
            center = Offset(w * 0.75f, h * 0.18f), radius = w * 0.55f,
        ),
        radius = w * 0.55f, center = Offset(w * 0.75f, h * 0.18f),
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFD4ECF8).copy(alpha = 0.30f * pulse), Color.Transparent),
            center = Offset(w * 0.15f, h * 0.75f), radius = w * 0.4f,
        ),
        radius = w * 0.4f, center = Offset(w * 0.15f, h * 0.75f),
    )

    // Ice particles
    particles.forEach { p ->
        val t = ((drift + p.offset) % 1f)
        val floatY = (p.y + drift * 0.03f * (p.offset - 0.5f)) % 1f
        val alpha = (p.brightness * 0.25f + t * 0.15f).coerceIn(0f, 0.4f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8CC8E8).copy(alpha = alpha), Color(0xFFADD8F0).copy(alpha = alpha * 0.3f), Color.Transparent),
                center = Offset(p.x * w, floatY * h), radius = p.size * 3f,
            ),
            radius = p.size * 3f, center = Offset(p.x * w, floatY * h),
        )
    }
}

private fun DrawScope.drawHalfSphere(isDark: Boolean, pulse: Float) {
    val w = size.width; val h = size.height
    val r = w * 1.1f
    val cx = w / 2f; val cy = h * 0.58f + r

    if (isDark) {
        for (i in 1..5) {
            val sw = (6 - i) * 4f
            val a = (0.06f - i * 0.01f).coerceAtLeast(0.005f) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(0.95f to Color(0xFF5EB0EF).copy(alpha = a), 1f to Color(0xFF5EB0EF).copy(alpha = a * 0.2f)),
                    center = Offset(cx, cy), radius = r + sw,
                ),
                radius = r + sw / 2f, center = Offset(cx, cy), style = Stroke(sw),
            )
        }

        drawCircle(
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0f to Color(0xFF5EB0EF).copy(alpha = 0.15f * pulse),
                    0.15f to Color(0xFF5EB0EF).copy(alpha = 0.06f * pulse),
                    0.35f to Color.Transparent,
                    0.5f to Color.Transparent,
                    0.65f to Color.Transparent,
                    0.85f to Color(0xFF5EB0EF).copy(alpha = 0.06f * pulse),
                    1f to Color(0xFF5EB0EF).copy(alpha = 0.15f * pulse),
                ),
                center = Offset(cx, cy),
            ),
            radius = r, center = Offset(cx, cy), style = Stroke(1.5f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Color(0xFF040408), 0.9f to Color(0xFF030306), 0.97f to Color(0xFF080810), 1f to Color(0xFF0C0C16)),
                center = Offset(cx, cy), radius = r,
            ),
            radius = r - 1f, center = Offset(cx, cy),
        )
    } else {
        for (i in 1..4) {
            val sw = (5 - i) * 6f
            val a = (0.12f - i * 0.025f).coerceAtLeast(0.01f) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(0.94f to Color(0xFF7AB8DC).copy(alpha = a), 1f to Color(0xFF9CCCE8).copy(alpha = a * 0.3f)),
                    center = Offset(cx, cy), radius = r + sw,
                ),
                radius = r + sw / 2f, center = Offset(cx, cy), style = Stroke(sw),
            )
        }

        drawCircle(
            brush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0f to Color(0xFF5AAAD0).copy(alpha = 0.25f * pulse),
                    0.15f to Color(0xFF80C0E0).copy(alpha = 0.12f * pulse),
                    0.35f to Color.Transparent,
                    0.5f to Color.Transparent,
                    0.65f to Color.Transparent,
                    0.85f to Color(0xFF80C0E0).copy(alpha = 0.12f * pulse),
                    1f to Color(0xFF5AAAD0).copy(alpha = 0.25f * pulse),
                ),
                center = Offset(cx, cy),
            ),
            radius = r, center = Offset(cx, cy), style = Stroke(1.5f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Color(0xFFECF2F8), 0.85f to Color(0xFFE6EEF4), 0.96f to Color(0xFFDEE8F0), 1f to Color(0xFFD6E2EC)),
                center = Offset(cx, cy), radius = r,
            ),
            radius = r - 1f, center = Offset(cx, cy),
        )
    }
}

@Composable
private fun SocialButton(iconRes: Int, text: String, isDark: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp).clip(shape)
            .then(
                if (isDark) Modifier.background(Color(0xFF0A0A0A)).border(0.8.dp, Color(0xFF1C1C1C), shape)
                else Modifier.background(Color.White).border(1.dp, Color(0xFFD8DEE6), shape)
            )
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), null, Modifier.size(22.dp), tint = Color.Unspecified)
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
            color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF222222))
    }
}

@Composable
private fun OrDivider(isDark: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(0.5.dp).background(if (isDark) Color(0xFF181818) else Color(0xFFD8DEE6)))
        Text("or", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color(0xFF3A3A3A) else Color(0xFFAAB0B8))
        Box(Modifier.weight(1f).height(0.5.dp).background(if (isDark) Color(0xFF181818) else Color(0xFFD8DEE6)))
    }
}
