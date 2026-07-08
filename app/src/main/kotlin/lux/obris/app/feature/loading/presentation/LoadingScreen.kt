package lux.obris.app.feature.loading.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Loading screen — background image fills every pixel.
 * Uses ContentScale.Crop to ensure edge-to-edge coverage.
 * Bottom gradient overlay for text readability.
 */
@Composable
fun LoadingScreen(
    statusMessages: List<String> = listOf("Loading..."),
    durationMs: Long = 3000L,
    onLoadingComplete: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var currentMessage by remember { mutableStateOf(statusMessages.first()) }

    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/vest_screen.png")
            .build(),
    )

    // Cycle messages
    LaunchedEffect(Unit) {
        val interval = durationMs / statusMessages.size
        for (msg in statusMessages) {
            currentMessage = msg
            delay(interval)
        }
    }

    // Progress
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMs.toInt(), easing = LinearEasing))
        delay(150)
        onLoadingComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Background image — Crop fills every pixel, no bars ever ──
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── Bottom gradient for readability ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                    startY = size.height * 0.5f,
                    endY = size.height,
                ),
                size = Size(size.width, size.height),
            )
        }

        // ── Bottom UI ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 40.dp, end = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = currentMessage.uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 2.sp,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(3.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(Color.White.copy(alpha = 0.15f), size = Size(size.width, size.height), cornerRadius = CornerRadius(2f))
                    val fw = size.width * progress.value
                    if (fw > 0f) {
                        drawRoundRect(Color.White.copy(alpha = 0.9f), size = Size(fw, size.height), cornerRadius = CornerRadius(2f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progress.value * 100).toInt()}%",
                style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f)),
            )
        }
    }
}
