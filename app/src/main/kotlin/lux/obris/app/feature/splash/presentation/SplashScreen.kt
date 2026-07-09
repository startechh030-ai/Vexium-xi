package lux.obris.app.feature.splash.presentation

import android.graphics.BitmapFactory
import android.media.MediaPlayer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random

/**
 * Splash screen — obris.png on black with cyberpunk glitch + sound.
 * 2.5 seconds total.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Load assets + play sound
    LaunchedEffect(Unit) {
        try {
            context.assets.open("splash/obris.png").use { stream ->
                logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: IOException) { e.printStackTrace() }

        try {
            mediaPlayer = MediaPlayer.create(context, lux.obris.app.R.raw.glitch)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) {}

        delay(2500)

        mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    // Glitch animation driver
    val infiniteTransition = rememberInfiniteTransition(label = "Glitch")
    val glitchTick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "GlitchTick",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        logoBitmap?.let { bitmap ->
            GlitchLogo(bitmap = bitmap, glitchTick = glitchTick)
        }
    }
}

/**
 * Draws the logo bitmap with cyberpunk glitch effect.
 * Slices the image horizontally, shifts slices, adds chromatic color artifacts.
 */
@Composable
private fun GlitchLogo(
    bitmap: ImageBitmap,
    glitchTick: Float,
    slices: Int = 16,
) {
    val glitchColors = listOf(Color(0xFF00FFFF), Color(0xFFFF0055), Color(0xFFFFFFFF))
    val glitchStep = glitchTick.toInt()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        // Center the logo — scale to ~35% of screen height
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
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(offsetX, offsetY),
                dstSize = IntSize(drawW, drawH),
            )
        } else {
            // Glitch frame — slice and shift
            val sliceH = drawH.toFloat() / slices

            for (i in 0 until slices) {
                val shiftX = if (Random.nextInt(4) < glitchStep) {
                    Random.nextInt(-45, 46).toFloat() * intensity
                } else 0f

                val applyColor = Random.nextInt(100) < 20
                val sliceTop = offsetY + i * sliceH
                val sliceBottom = offsetY + (i + 1) * sliceH

                clipRect(
                    left = 0f,
                    top = sliceTop,
                    right = canvasW,
                    bottom = sliceBottom,
                ) {
                    translate(left = shiftX) {
                        drawImage(
                            image = bitmap,
                            dstOffset = IntOffset(offsetX, offsetY),
                            dstSize = IntSize(drawW, drawH),
                        )
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
