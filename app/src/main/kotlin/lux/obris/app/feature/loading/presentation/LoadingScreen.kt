package lux.obris.app.feature.loading.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
 * Loading screen — shows vest_screen.png background with a progress bar.
 * Simulates loading tasks (placeholder for real loading later).
 *
 * @param statusMessages List of loading status messages to cycle through.
 * @param durationMs Total loading time in milliseconds.
 * @param onLoadingComplete Called when loading finishes.
 */
@Composable
fun LoadingScreen(
    statusMessages: List<String> = listOf(
        "Initializing...",
        "Loading assets...",
        "Connecting to server...",
        "Preparing the world...",
        "Almost ready...",
    ),
    durationMs: Long = 3000L,
    onLoadingComplete: () -> Unit,
) {
    // ── Progress animation ──
    val progress = remember { Animatable(0f) }
    var messageIndex by remember { mutableIntStateOf(0) }
    var currentMessage by remember { mutableStateOf(statusMessages.first()) }

    // ── Background image ──
    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/vest_screen.png")
            .build(),
    )

    // ── Animate progress and cycle messages ──
    LaunchedEffect(Unit) {
        // Animate progress bar
        progress.animateTo(1f, tween(durationMs.toInt(), easing = LinearEasing))
        delay(200)
        onLoadingComplete()
    }

    // Cycle status messages
    LaunchedEffect(Unit) {
        val interval = durationMs / statusMessages.size
        for (i in statusMessages.indices) {
            currentMessage = statusMessages[i]
            messageIndex = i
            delay(interval)
        }
    }

    // ── UI ──
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image — stretch to fill
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Bottom overlay — loading bar + text
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // "Loading..." text
            Text(
                text = "Loading...",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 2.sp,
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar + percentage
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(4.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Track
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(h / 2f),
                    )

                    // Fill
                    val fillWidth = w * progress.value
                    if (fillWidth > 0f) {
                        drawRoundRect(
                            color = Color.White,
                            size = Size(fillWidth, h),
                            cornerRadius = CornerRadius(h / 2f),
                        )
                    }
                }

                // Percentage text
                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(start = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status message
            Text(
                text = currentMessage,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}
