package lux.obris.app.feature.welcome.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import lux.obris.app.R

/**
 * Welcome screen — vest_screen.png background, full screen.
 * Auth buttons on the right side (landscape).
 * No UI polish yet — placeholder layout.
 */
@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    // Background image from assets
    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/vest_screen.png")
            .build(),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Background — stretch to fill ──
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── Auth buttons — right side for landscape ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left spacer — logo area is in the background image
            Spacer(modifier = Modifier.weight(1f))

            // Right — auth buttons
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Google
                WelcomeButton(
                    iconRes = R.drawable.ic_google,
                    text = "Continue with Google",
                    onClick = onGoogleClick,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                WelcomeButton(
                    iconRes = R.drawable.ic_email,
                    text = "Continue with Email",
                    onClick = onEmailClick,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Guest
                Text(
                    text = "Play as Guest",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            remember { MutableInteractionSource() },
                            ripple(bounded = true),
                            onClick = onGuestClick,
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legal
                Text(
                    text = "By continuing, you agree to our Terms & Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

/** Auth button — semi-transparent with border */
@Composable
private fun WelcomeButton(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(48.dp)
            .clip(shape)
            .border(
                0.8.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)),
                ),
                shape,
            )
            .clickable(
                remember { MutableInteractionSource() },
                ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(iconRes),
            null,
            Modifier.size(18.dp),
            tint = Color.Unspecified,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}
