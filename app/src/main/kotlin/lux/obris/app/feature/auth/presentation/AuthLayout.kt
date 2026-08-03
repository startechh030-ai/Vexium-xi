package lux.obris.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.obris.app.R

// ── Colors ──
private val NeonGreen = Color(0xFF00FF66)
private val ElectricCyan = Color(0xFF00E5FF)
private val HotPink = Color(0xFFFF2D78)
private val GlassWhite = Color(0xFFFFFFFF)
private val GlassBg = Color(0xFF000000)

/**
 * Auth layout — cyberpunk glassmorphism style.
 * "RIFT RAGERS" logo text at center.
 * Glassmorphic auth buttons at bottom.
 * Side toolbar icons at top-right.
 * Version info at top-right.
 */
@Composable
fun AuthLayout(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── "RIFT RAGERS" logo text — center of screen ──
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // "Rift" — smaller, italic, cyan outline feel
            Text(
                text = "RIFT",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = ElectricCyan,
                    letterSpacing = 12.sp,
                    fontFamily = FontFamily.Monospace,
                ),
            )
            // "RAGERS" — big, bold, neon green glow
            Text(
                text = "RAGERS",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = NeonGreen,
                    letterSpacing = 6.sp,
                ),
            )
        }

        // ── Side toolbar — top right (like Arena Breakout) ──
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SideIcon(Icons.Filled.Build, "Settings")
            SideIcon(Icons.Filled.VolumeUp, "Audio")
            SideIcon(Icons.Filled.Headphones, "Support")
            SideIcon(Icons.Filled.Language, "Language")
        }

        // ── Version — top right below icons ──
        Text(
            text = "v1.0.0",
            style = TextStyle(
                fontSize = 7.sp,
                color = Color.White.copy(alpha = 0.20f),
                fontFamily = FontFamily.Monospace,
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 8.dp),
        )

        // ── Auth buttons — bottom center, glassmorphism ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Guest — green glass
                GlassBtn(
                    text = "Guest",
                    accentColor = NeonGreen,
                    textColor = Color.White,
                    onClick = onGuestClick,
                )

                // Google — white glass
                GlassBtn(
                    text = "Sign in with Google",
                    accentColor = GlassWhite,
                    textColor = Color(0xFF1A1A1A),
                    iconRes = R.drawable.ic_google,
                    onClick = onGoogleClick,
                )

                // Email — cyan glass
                GlassBtn(
                    text = "Sign in with Email",
                    accentColor = ElectricCyan,
                    textColor = Color.White,
                    iconRes = R.drawable.ic_email,
                    iconTint = Color.White,
                    onClick = onEmailClick,
                )

                // More — dark glass
                GlassBtn(
                    text = "More",
                    accentColor = Color(0xFF888888),
                    textColor = Color(0xFFDDDDDD),
                    onClick = onMoreClick,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "By continuing, you agree to the Terms of Service and Privacy Policy.",
                style = TextStyle(fontSize = 7.sp, color = Color.White.copy(alpha = 0.15f)),
            )
        }
    }
}

/** Glassmorphism button — frosted glass with color accent border */
@Composable
private fun GlassBtn(
    text: String,
    accentColor: Color,
    textColor: Color,
    iconRes: Int? = null,
    iconTint: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val isWhite = accentColor == GlassWhite

    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .background(
                if (isWhite) {
                    // White button — solid white with slight transparency
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.85f))
                    )
                } else {
                    // Glass effect — dark with colored tint
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.25f),
                            accentColor.copy(alpha = 0.12f),
                        )
                    )
                }
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = if (isWhite) 0.3f else 0.5f),
                        accentColor.copy(alpha = if (isWhite) 0.1f else 0.15f),
                    )
                ),
                shape = shape,
            )
            .clickable(
                remember { MutableInteractionSource() },
                ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (iconRes != null) {
            Icon(
                painterResource(iconRes),
                null,
                Modifier.size(14.dp),
                tint = iconTint,
            )
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.5.sp,
            ),
        )
    }
}

/** Side toolbar icon — small circular button */
@Composable
private fun SideIcon(icon: ImageVector, label: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable { /* placeholder */ },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, label,
            Modifier.size(14.dp),
            tint = Color.White.copy(alpha = 0.6f),
        )
    }
}
