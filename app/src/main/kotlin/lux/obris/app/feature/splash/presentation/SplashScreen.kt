package lux.obris.app.feature.splash.presentation

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random

/**
 * Splash screen — loads obris.png from assets/splash/,
 * plays glitch.mp3 sound, applies cyberpunk glitch animation.
 * Pure black background. 2.5 seconds total.
 *
 * Flow: Tap app → black → logo appears with glitch → done
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Load assets + play sound
    LaunchedEffect(Unit) {
        // Load logo from assets/splash/obris.png
        try {
            context.assets.open("splash/obris.png").use { stream ->
                logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Play glitch sound from res/raw/glitch.mp3
        try {
            mediaPlayer = MediaPlayer.create(context, lux.obris.app.R.raw.glitch)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Total splash duration: 2.5 seconds
        delay(2500)

        // Cleanup and navigate
        mediaPlayer?.let {
            try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {}
        }
        onSplashFinished()
    }

    // Release on dispose (safety)
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let {
                try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {}
            }
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)), // Pure black
        contentAlignment = Alignment.Center,
    ) {
        logoBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Obris Logo",
                modifier = Modifier
                    .size(240.dp)
                    .cyberpunkGlitch(enabled = true),
            )
        }
    }
}

/**
 * Cyberpunk glitch modifier — slices the image horizontally,
 * shifts slices randomly, overlays chromatic color artifacts.
 */
@Composable
fun Modifier.cyberpunkGlitch(
    enabled: Boolean = true,
    slices: Int = 16,
    glitchColors: List<Color> = listOf(Color(0xFF00FFFF), Color(0xFFFF0055), Color(0xFFFFFFFF)),
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "Glitch")

    // Tick that drives the glitch timing
    val glitchStep by infiniteTransition.animateInt(
        initialValue = 0,
        targetValue = 10,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "GlitchStep",
    )

    return this
        .graphicsLayer { clip = true }
        .drawWithContent {
            // Only glitch during certain frames (2-6 out of 0-10)
            val isGlitchActive = glitchStep in 2..6
            val intensity = if (isGlitchActive) (glitchStep / 10f) else 0f

            if (!isGlitchActive) {
                // Clean frame — just draw normally
                drawContent()
            } else {
                // Glitch frame — slice and shift
                val sliceHeight = size.height / slices

                for (i in 0 until slices) {
                    // Random horizontal shift per slice
                    val horizontalShift = if (Random.nextInt(4) < glitchStep) {
                        Random.nextInt(-45, 46).toFloat() * intensity
                    } else 0f

                    // Random chance of color overlay
                    val applyColor = Random.nextInt(100) < 20

                    clipRect(
                        top = i * sliceHeight,
                        bottom = (i + 1) * sliceHeight,
                    ) {
                        translate(left = horizontalShift) {
                            drawContent()

                            if (applyColor) {
                                drawRect(
                                    color = glitchColors[Random.nextInt(glitchColors.size)],
                                    topLeft = Offset(0f, i * sliceHeight),
                                    size = Size(size.width, sliceHeight),
                                    blendMode = BlendMode.SrcAtop,
                                )
                            }
                        }
                    }
                }
            }
        }
}
