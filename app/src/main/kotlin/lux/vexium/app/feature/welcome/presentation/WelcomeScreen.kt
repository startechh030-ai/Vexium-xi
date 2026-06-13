package lux.vexium.app.feature.welcome.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.vexium.app.core.components.VexiumLogo
import lux.vexium.app.core.theme.GoogleBlue
import lux.vexium.app.core.theme.GoogleGreen
import lux.vexium.app.core.theme.GoogleRed
import lux.vexium.app.core.theme.GoogleYellow
import lux.vexium.app.core.theme.TelegramBlue

@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onTelegramClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Background globe/sphere glow ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGlobe(isDark = isDark)
        }

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Push logo to upper-center area
            Spacer(modifier = Modifier.weight(1f))

            // ── Vexium Logo ──
            VexiumLogo(isDark = isDark)

            Spacer(modifier = Modifier.weight(1.2f))

            // ── Auth Buttons ──
            // Google
            WelcomeButton(
                text = "Continue With Google",
                icon = { GoogleIcon() },
                isDark = isDark,
                onClick = onGoogleClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Telegram
            WelcomeButton(
                text = "Continue With Telegram",
                icon = { TelegramIcon(isDark = isDark) },
                isDark = isDark,
                onClick = onTelegramClick,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            WelcomeButton(
                text = "Continue With Email",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isDark) {
                            Color.White.copy(alpha = 0.7f)
                        } else {
                            Color(0xFF5F6368)
                        },
                    )
                },
                isDark = isDark,
                onClick = onEmailClick,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Try as Guest
            TextButton(onClick = onGuestClick) {
                Text(
                    text = "Try as Guest",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ══════════════════════════════════════
//  WELCOME BUTTON
// ══════════════════════════════════════
@Composable
private fun WelcomeButton(
    text: String,
    icon: @Composable () -> Unit,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDark) {
                Color.White.copy(alpha = 0.08f)
            } else {
                Color.Black.copy(alpha = 0.04f)
            },
            contentColor = if (isDark) {
                Color.White.copy(alpha = 0.9f)
            } else {
                Color(0xFF1A1C1E)
            },
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f))
                } else {
                    listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.05f))
                },
            ),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ══════════════════════════════════════
//  GLOBE / SPHERE GLOW EFFECT
// ══════════════════════════════════════
private fun DrawScope.drawGlobe(isDark: Boolean) {
    val width = size.width
    val height = size.height

    // Globe sits in the lower-center area, behind buttons
    val globeCenterX = width / 2f
    val globeCenterY = height * 0.62f
    val globeRadius = width * 0.7f

    if (isDark) {
        // Dark mode: subtle white/blue sphere glow
        // Outer ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.02f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY),
                radius = globeRadius,
            ),
            radius = globeRadius,
            center = Offset(globeCenterX, globeCenterY),
        )

        // Bright horizon line (the crescent of light at top of the sphere)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY - globeRadius * 0.3f),
                radius = globeRadius * 0.6f,
            ),
            radius = globeRadius * 0.6f,
            center = Offset(globeCenterX, globeCenterY - globeRadius * 0.3f),
        )

        // Top edge highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY - globeRadius * 0.45f),
                radius = globeRadius * 0.35f,
            ),
            radius = globeRadius * 0.35f,
            center = Offset(globeCenterX, globeCenterY - globeRadius * 0.45f),
        )
    } else {
        // Light mode: subtle blue-white atmospheric glow
        // Outer soft atmosphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFD4F0FF).copy(alpha = 0.25f),
                    Color(0xFFE8F6FF).copy(alpha = 0.12f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY),
                radius = globeRadius,
            ),
            radius = globeRadius,
            center = Offset(globeCenterX, globeCenterY),
        )

        // Inner bright core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFB2E8FF).copy(alpha = 0.18f),
                    Color(0xFFD4F0FF).copy(alpha = 0.08f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY - globeRadius * 0.3f),
                radius = globeRadius * 0.5f,
            ),
            radius = globeRadius * 0.5f,
            center = Offset(globeCenterX, globeCenterY - globeRadius * 0.3f),
        )

        // Horizon glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.30f),
                    Color(0xFFE0F4FF).copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                center = Offset(globeCenterX, globeCenterY - globeRadius * 0.45f),
                radius = globeRadius * 0.30f,
            ),
            radius = globeRadius * 0.30f,
            center = Offset(globeCenterX, globeCenterY - globeRadius * 0.45f),
        )
    }
}

// ══════════════════════════════════════
//  GOOGLE ICON (multi-color G)
// ══════════════════════════════════════
@Composable
private fun GoogleIcon() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f

        // Simplified Google "G" using colored arcs
        drawArc(
            color = GoogleRed,
            startAngle = -30f,
            sweepAngle = -120f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        )
        drawArc(
            color = GoogleYellow,
            startAngle = -150f,
            sweepAngle = -60f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        )
        drawArc(
            color = GoogleGreen,
            startAngle = -210f,
            sweepAngle = -60f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        )
        drawArc(
            color = GoogleBlue,
            startAngle = -270f,
            sweepAngle = -90f,
            useCenter = true,
            topLeft = Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        )
        // Center cutout
        drawCircle(
            color = Color.White,
            radius = r * 0.55f,
            center = Offset(cx, cy),
        )
        // Horizontal bar of the G
        drawRect(
            color = GoogleBlue,
            topLeft = Offset(cx, cy - r * 0.15f),
            size = androidx.compose.ui.geometry.Size(r * 0.9f, r * 0.30f),
        )
    }
}

// ══════════════════════════════════════
//  TELEGRAM ICON
// ══════════════════════════════════════
@Composable
private fun TelegramIcon(isDark: Boolean) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height

        // Circle background
        drawCircle(
            color = TelegramBlue,
            radius = w / 2f,
            center = Offset(w / 2f, h / 2f),
        )

        // Paper plane shape (simplified triangle)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.80f, h * 0.28f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.40f, h * 0.58f)
            close()
        }
        drawPath(path = path, color = Color.White)

        // Inner fold
        val fold = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.40f, h * 0.58f)
            lineTo(w * 0.60f, h * 0.70f)
            lineTo(w * 0.80f, h * 0.28f)
            lineTo(w * 0.45f, h * 0.75f)
            close()
        }
        drawPath(path = fold, color = Color.White.copy(alpha = 0.85f))
    }
}
