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
import kotlin.random.Random

/**
 * Splash — logo + glitch + distant sound.
 * Sound starts at 0.4s, 30% volume, distant feel.
 * Simple fade out at end. No curtain.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val fadeOut = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Load logo immediately on IO
        launch(Dispatchers.IO) {
            try {
                context.assets.open("splash/obris.png").use { stream ->
                    logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {}
        }

        // Wait 0.4 seconds before starting sound
        delay(400)

        // Create and start sound — 30% volume, distant
        val player = try {
            MediaPlayer.create(context, lux.obris.app.R.raw.glitch)?.apply {
                setVolume(0.30f, 0.30f)
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) { null }
        mediaPlayer = player

        // Wait for sound to finish
        val soundDuration = player?.duration?.toLong() ?: 1600L
        delay(soundDuration)

        // Simple fade out
        fadeOut.animateTo(0f, tween(350))

        // Navigate
        mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    // Glitch tick
    val inf = rememberInfiniteTransition(label = "G")
    val glitchTick by inf.animateFloat(
        0f, 10f,
        infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "t",
    )

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        logoBitmap?.let { bitmap ->
            GlitchLogo(bitmap, glitchTick, fadeOut.value)
        }
    }
}

@Composable
private fun GlitchLogo(bitmap: ImageBitmap, glitchTick: Float, alpha: Float) {
    val colors = listOf(Color(0xFF00FFFF), Color(0xFFFF0055), Color(0xFFFFFFFF))
    val step = glitchTick.toInt()

    Canvas(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
        val w = size.width; val h = size.height
        val tH = h * 0.35f
        val sc = tH / bitmap.height
        val dW = (bitmap.width * sc).toInt()
        val dH = (bitmap.height * sc).toInt()
        val oX = ((w - dW) / 2f).toInt()
        val oY = ((h - dH) / 2f).toInt()

        val active = step in 2..6
        val intensity = if (active) step / 10f else 0f

        if (!active) {
            drawImage(bitmap, dstOffset = IntOffset(oX, oY), dstSize = IntSize(dW, dH))
        } else {
            val sliceH = dH / 16f
            for (i in 0 until 16) {
                val shift = if (Random.nextInt(4) < step) Random.nextInt(-45, 46) * intensity else 0f
                val top = oY + i * sliceH
                val bot = oY + (i + 1) * sliceH
                clipRect(0f, top, w, bot) {
                    translate(left = shift) {
                        drawImage(bitmap, dstOffset = IntOffset(oX, oY), dstSize = IntSize(dW, dH))
                    }
                    if (Random.nextInt(100) < 20) {
                        drawRect(colors[Random.nextInt(3)], Offset(oX + shift, top), Size(dW.toFloat(), sliceH), blendMode = BlendMode.SrcAtop)
                    }
                }
            }
        }
    }
}
