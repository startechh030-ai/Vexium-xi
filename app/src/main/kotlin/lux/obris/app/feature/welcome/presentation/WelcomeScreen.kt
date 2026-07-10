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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import lux.obris.app.feature.auth.presentation.AuthLayout

/**
 * Welcome screen — full bleed background + AuthLayout overlay.
 * AuthLayout is a separate composable for easy editing.
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

        // Version top-left
        Text(
            text = "v1.0.0",
            style = TextStyle(fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f), letterSpacing = 1.sp),
            modifier = Modifier.padding(start = 12.dp, top = 8.dp).align(Alignment.TopStart),
        )

        // Auth overlay — bottom 40%
        AuthLayout(
            onGoogleClick = onGoogleClick,
            onEmailClick = onEmailClick,
            onGuestClick = onGuestClick,
            onMoreClick = onEmailClick, // More → email for now
        )
    }
}
