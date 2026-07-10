package lux.obris.app.feature.loading.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Loading screen — background fills edge to edge.
 * Progress bar at bottom-left, small (40% width).
 * Status text right-aligned at bar start with scan line animation.
 * Percentage at bar end.
 */
@Composable
fun LoadingScreen(
    backgroundImage: String = "loading_bg_1.jpg",
    statusMessages: List<String> = listOf("Loading..."),
    durationMs: Long = 3000L,
    onLoadingComplete: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var currentMessage by remember { mutableStateOf(statusMessages.first()) }

    // Scan line animation — moves left to right across the text
    val inf = rememberInfiniteTransition(label = "scan")
    val scanPos by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLine",
    )

    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/screens/$backgroundImage")
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
        // Background
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Bottom gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                    startY = size.height * 0.55f,
                    endY = size.height,
                ),
                size = Size(size.width, size.height),
            )
        }

        // ── Bottom loading UI — positioned at very bottom ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, end = 28.dp, bottom = 14.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Status text with scan line + transparent border
                Box(contentAlignment = Alignment.CenterStart) {
                    // Text with monospace feel
                    Text(
                        text = currentMessage.uppercase(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                    )

                    // Scan line overlay — thin vertical line sweeps across text
                    Canvas(modifier = Modifier.width(150.dp).height(14.dp)) {
                        val lineX = size.width * scanPos
                        // Transparent border edges
                        drawRect(
                            color = Color.White.copy(alpha = 0.04f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                        )
                        // Scan line
                        drawLine(
                            color = Color(0xFF7DD3FC).copy(alpha = 0.6f),
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, size.height),
                            strokeWidth = 1.5f,
                        )
                        // Glow around scan line
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF7DD3FC).copy(alpha = 0.08f), Color.Transparent),
                                startX = (lineX - 15f).coerceAtLeast(0f),
                                endX = (lineX + 15f).coerceAtMost(size.width),
                            ),
                            size = Size(size.width, size.height),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Progress bar — 40% width
                Box(modifier = Modifier.weight(0.4f).height(3.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(Color.White.copy(alpha = 0.10f), size = Size(size.width, size.height), cornerRadius = CornerRadius(2f))
                        val fw = size.width * progress.value
                        if (fw > 0f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF7DD3FC), Color.White)),
                                size = Size(fw, size.height),
                                cornerRadius = CornerRadius(2f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Percentage
                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace),
                )

                // Fill remaining space
                Spacer(modifier = Modifier.weight(0.6f))
            }
        }
    }
}
