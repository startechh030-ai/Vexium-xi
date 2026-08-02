package lux.obris.app.feature.welcome.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import lux.obris.app.feature.auth.presentation.AuthLayout

/**
 * Welcome — full bleed background with compact AuthLayout at bottom.
 * Auth buttons are small and don't cover the artwork.
 */
@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/screens/loading_bg_1.jpg")
            .build(),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background — edge to edge
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Version — top right (like Arena Breakout)
        Text(
            text = "1.0.0 | Performance: Stable",
            style = TextStyle(
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.25f),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 8.dp),
        )

        // Auth — compact buttons at bottom
        AuthLayout(
            onGoogleClick = onGoogleClick,
            onEmailClick = onEmailClick,
            onGuestClick = onGuestClick,
            onMoreClick = onEmailClick,
        )
    }
}
