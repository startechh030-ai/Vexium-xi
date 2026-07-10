package lux.obris.app.feature.splash.presentation

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Splash — obris.png + glitch.mp3 start at the SAME time.
 * Sound and visual are perfectly synced.
 * 2 seconds: 1s forward glitch, 1s reverse settle.
 * Then 400ms fade out → navigate.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val fadeOut = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Pre-create MediaPlayer IMMEDIATELY — no coroutine overhead
        val player = try {
            MediaPlayer.create(context, lux.obris.app.R.raw.glitch)
        } catch (_: Exception) { null }
        mediaPlayer = player

        // Load logo on IO thread (fast — local asset)
        launch(Dispatchers.IO) {
            try {
                context.assets.open("splash/obris.png").use { stream ->
                    logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {}
        }

        // Start sound NOW — no delay
        player?.apply {
            setOnCompletionListener { it.release() }
            start()
        }

        // Wait for the sound duration (2 seconds)
        delay(1600)

        // Fade out
        fadeOut.animateTo(0f, tween(400))

        // Cleanup
        mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    // Glitch animation — 1s forward, 1s reverse (matches sound)
    val infiniteTransition = rememberInfiniteTransition(label = "Glitch")
    val glitchTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "GlitchTick",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        logoBitmap?.let { bitmap ->
            GlitchLogo(bitmap = bitmap, glitchTick = glitchTick, alpha = fadeOut.value)
        }
    }
}

/** Draws the logo with cyberpunk glitch — slices + chromatic artifacts */
@Composable
private fun GlitchLogo(
    bitmap: ImageBitmap,
    glitchTick: Float,
    alpha: Float = 1f,
    slices: Int = 16,
) {
    val glitchColors = listOf(Color(0xFF00FFFF), Color(0xFFFF0055), Color(0xFFFFFFFF))
    val glitchStep = glitchTick.toInt()

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
        val canvasW = size.width
        val canvasH = size.height

        // Center and scale logo to ~35% of screen height
        val targetH = canvasH * 0.35f
        val scale = targetH / bitmap.height.toFloat()
        val drawW = (bitmap.width * scale).toInt()
        val drawH = (bitmap.height * scale).toInt()
        val offsetX = ((canvasW - drawW) / 2f).toInt()
        val offsetY = ((canvasH - drawH) / 2f).toInt()

        val isGlitchActive = glitchStep in 2..6
        val intensity = if (isGlitchActive) (glitchStep / 10f) else 0f

        if (!isGlitchActive) {
            // Clean frame
            drawImage(bitmap, dstOffset = IntOffset(offsetX, offsetY), dstSize = IntSize(drawW, drawH))
        } else {
            // Glitch frame
            val sliceH = drawH.toFloat() / slices

            for (i in 0 until slices) {
                val shiftX = if (Random.nextInt(4) < glitchStep) {
                    Random.nextInt(-45, 46).toFloat() * intensity
                } else 0f

                val applyColor = Random.nextInt(100) < 20
                val sliceTop = offsetY + i * sliceH
                val sliceBottom = offsetY + (i + 1) * sliceH

                clipRect(left = 0f, top = sliceTop, right = canvasW, bottom = sliceBottom) {
                    translate(left = shiftX) {
                        drawImage(bitmap, dstOffset = IntOffset(offsetX, offsetY), dstSize = IntSize(drawW, drawH))
                    }

                    if (applyColor) {
                        drawRect(
                            color = glitchColors[Random.nextInt(glitchColors.size)],
                            topLeft = Offset(offsetX.toFloat() + shiftX, sliceTop),
                            size = Size(drawW.toFloat(), sliceH),
                            blendMode = BlendMode.SrcAtop,
                        )
                    }
                }
            }
        }
    }
}
