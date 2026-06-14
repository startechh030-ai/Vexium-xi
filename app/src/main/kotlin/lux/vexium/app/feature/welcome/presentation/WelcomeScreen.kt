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
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF050508),
                            Color(0xFF080A10),
                            Color(0xFF050508),
                        ),
                    )
                } else {
                    // Light: subtle blue-tinted atmosphere
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFB),
                            Color(0xFFF0F6FA),
                            Color(0xFFE8F0F6),
                            Color(0xFFF0F6FA),
                            Color(0xFFF8FAFB),
                        ),
                    )
                },
            ),
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
            Spacer(modifier = Modifier.weight(0.9f))

            // ── Vexium Logo ──
            VexiumLogo(isDark = isDark)

            Spacer(modifier = Modifier.weight(1.4f))

            // ── Auth Buttons ──
            WelcomeButton(
                text = "Continue With Google",
                icon = { GoogleIcon() },
                isDark = isDark,
                onClick = onGoogleClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            WelcomeButton(
                text = "Continue With Telegram",
                icon = { TelegramIcon() },
                isDark = isDark,
                onClick = onTelegramClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            WelcomeButton(
                text = "Continue With Email",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF5F6368),
                    )
                },
                isDark = isDark,
                onClick = onEmailClick,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Try as Guest
            TextButton(onClick = onGuestClick) {
                Text(
                    text = "Try as Guest",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) {
                        Color.White.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    },
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
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
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDark) {
                Color.White.copy(alpha = 0.06f)
            } else {
                Color.White.copy(alpha = 0.85f)
            },
            contentColor = if (isDark) {
                Color.White.copy(alpha = 0.85f)
            } else {
                Color(0xFF1A1C1E)
            },
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (isDark) 0.8.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = if (isDark) {
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.05f))
                } else {
                    listOf(Color(0xFFD0D5DC), Color(0xFFE0E4E8))
                },
            ),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isDark) 0.dp else 1.dp,
        ),
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
    val w = size.width
    val h = size.height

    // Globe center — between logo and buttons area
    val cx = w / 2f
    val cy = h * 0.58f
    val radius = w * 0.65f

    if (isDark) {
        // Dark: subtle warm white dome glow

        // Very soft outer haze
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.025f),
                    Color.White.copy(alpha = 0.01f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = radius * 1.2f,
            ),
            radius = radius * 1.2f,
            center = Offset(cx, cy),
        )

        // Bright crescent at top of the sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy - radius * 0.35f),
                radius = radius * 0.5f,
            ),
            radius = radius * 0.5f,
            center = Offset(cx, cy - radius * 0.35f),
        )

        // Tight top highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy - radius * 0.50f),
                radius = radius * 0.28f,
            ),
            radius = radius * 0.28f,
            center = Offset(cx, cy - radius * 0.50f),
        )
    } else {
        // Light: soft blue atmospheric globe (like the mockup)

        // Large outer atmosphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFCCE8F8).copy(alpha = 0.40f),
                    Color(0xFFDCEFF8).copy(alpha = 0.20f),
                    Color(0xFFF0F6FA).copy(alpha = 0.05f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = radius * 1.1f,
            ),
            radius = radius * 1.1f,
            center = Offset(cx, cy),
        )

        // Inner brighter core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFB8E0F4).copy(alpha = 0.35f),
                    Color(0xFFCCE8F8).copy(alpha = 0.15f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy - radius * 0.20f),
                radius = radius * 0.55f,
            ),
            radius = radius * 0.55f,
            center = Offset(cx, cy - radius * 0.20f),
        )

        // Top crescent highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.50f),
                    Color(0xFFDCEFF8).copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                center = Offset(cx, cy - radius * 0.42f),
                radius = radius * 0.25f,
            ),
            radius = radius * 0.25f,
            center = Offset(cx, cy - radius * 0.42f),
        )
    }
}

// ══════════════════════════════════════
//  GOOGLE ICON (multi-color G)
// ══════════════════════════════════════
@Composable
private fun GoogleIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val s = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        val tl = Offset(cx - r, cy - r)

        drawArc(color = GoogleRed, startAngle = -30f, sweepAngle = -120f, useCenter = true, topLeft = tl, size = s)
        drawArc(color = GoogleYellow, startAngle = -150f, sweepAngle = -60f, useCenter = true, topLeft = tl, size = s)
        drawArc(color = GoogleGreen, startAngle = -210f, sweepAngle = -60f, useCenter = true, topLeft = tl, size = s)
        drawArc(color = GoogleBlue, startAngle = -270f, sweepAngle = -90f, useCenter = true, topLeft = tl, size = s)
        drawCircle(color = Color.White, radius = r * 0.55f, center = Offset(cx, cy))
        drawRect(color = GoogleBlue, topLeft = Offset(cx, cy - r * 0.15f), size = androidx.compose.ui.geometry.Size(r * 0.9f, r * 0.30f))
    }
}

// ══════════════════════════════════════
//  TELEGRAM ICON
// ══════════════════════════════════════
@Composable
private fun TelegramIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(color = TelegramBlue, radius = w / 2f, center = Offset(w / 2f, h / 2f))

        val plane = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.80f, h * 0.28f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.40f, h * 0.58f)
            close()
        }
        drawPath(path = plane, color = Color.White)

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
