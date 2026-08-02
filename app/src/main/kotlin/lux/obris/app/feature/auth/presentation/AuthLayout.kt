package lux.obris.app.feature.auth.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.obris.app.R

// ── Button colors — each auth method gets a distinct color ──
private val GuestGreen = Color(0xFF2ECC40)
private val GoogleWhite = Color(0xFFF5F5F5)
private val EmailCyan = Color(0xFF00BCD4)
private val MoreGrey = Color(0xFF3A3A42)

/**
 * Auth layout — compact row of buttons at bottom center.
 * Inspired by Arena Breakout — small, colorful, doesn't cover artwork.
 * Transparent background — sits over the welcome screen image.
 */
@Composable
fun AuthLayout(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            // ── Auth buttons row ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Guest — green
                AuthBtn(
                    text = "Guest",
                    bgColor = GuestGreen,
                    textColor = Color.White,
                    icon = null,
                    onClick = onGuestClick,
                )

                // Google — white
                AuthBtn(
                    text = "Sign in with Google",
                    bgColor = GoogleWhite,
                    textColor = Color(0xFF333333),
                    iconRes = R.drawable.ic_google,
                    onClick = onGoogleClick,
                )

                // Email — cyan
                AuthBtn(
                    text = "Sign in with Email",
                    bgColor = EmailCyan,
                    textColor = Color.White,
                    iconRes = R.drawable.ic_email,
                    iconTint = Color.White,
                    onClick = onEmailClick,
                )

                // More — dark grey
                AuthBtn(
                    text = "More",
                    bgColor = MoreGrey,
                    textColor = Color(0xFFCCCCCC),
                    icon = null,
                    onClick = onMoreClick,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Legal — tiny, almost invisible
            Text(
                "By continuing, you agree to the Terms of Service and Privacy Policy.",
                style = TextStyle(fontSize = 7.sp, color = Color.White.copy(alpha = 0.18f)),
            )
        }
    }
}

/** Single auth button — compact, colored */
@Composable
private fun AuthBtn(
    text: String,
    bgColor: Color,
    textColor: Color,
    iconRes: Int? = null,
    icon: Unit? = Unit, // null = no icon placeholder
    iconTint: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(
                remember { MutableInteractionSource() },
                ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
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
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                letterSpacing = 0.3.sp,
            ),
        )
    }
}
