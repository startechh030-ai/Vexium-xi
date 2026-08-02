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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * Splash — "Rift Ragers" logo on black.
 * 3 seconds total.
 *
 * Timeline:
 *   0.0s — Sound starts (distant, 20% volume)
 *   0.2s — Logo fades in with heavy flicker
 *   0.2-1.5s — Intense glitch bursts synced to sound hits
 *   1.5-2.5s — Glitch settles, logo stabilizes
 *   2.5-3.0s — Clean hold then fade out
 *
 * Sound starts FIRST. Visual follows 200ms later.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val fadeOut = remember { Animatable(1f) }

    // Flicker control — drives visibility on/off rapidly
    var flickerAlpha by remember { mutableFloatStateOf(0f) }

    // Glitch intensity — 0=clean, 1=maximum chaos
    var glitchPower by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        // Load logo on IO (fast, local asset)
        launch(Dispatchers.IO) {
            try {
                context.assets.open("splash/obris.png").use { stream ->
                    logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {}
        }

        // ── SOUND STARTS FIRST ──
        val player = try {
            MediaPlayer.create(context, lux.obris.app.R.raw.glitch)?.apply {
                setVolume(0.20f, 0.20f) // 20% — distant
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) { null }
        mediaPlayer = player

        // 200ms gap — sound plays alone in darkness
        delay(200)

        // ── VISUAL STARTS — intense flicker entrance ──
        // Rapid on/off flicker (like a broken signal catching)
        repeat(6) {
            flickerAlpha = 1f; delay(40)
            flickerAlpha = 0f; delay(30)
        }
        flickerAlpha = 1f

        // ── GLITCH BURST 1 — heavy (0.6s-1.0s) ──
        glitchPower = 0.8f; delay(80)
        glitchPower = 0.2f; delay(50)
        glitchPower = 1.0f; delay(100)
        flickerAlpha = 0f; delay(30) // blackout frame
        flickerAlpha = 1f
        glitchPower = 0.5f; delay(60)
        glitchPower = 0f; delay(80)

        // ── GLITCH BURST 2 — double tap (1.0s-1.3s) ──
        glitchPower = 0.7f; delay(50)
        glitchPower = 0f; delay(40)
        glitchPower = 0.9f; delay(60)
        flickerAlpha = 0.3f; delay(25)
        flickerAlpha = 1f
        glitchPower = 1.0f; delay(70)
        glitchPower = 0f; delay(50)

        // ── GLITCH BURST 3 — signal breakdown (1.3s-1.5s) ──
        flickerAlpha = 0f; delay(20)
        flickerAlpha = 1f; glitchPower = 1f; delay(40)
        flickerAlpha = 0.1f; delay(15)
        flickerAlpha = 1f; glitchPower = 0.6f; delay(35)
        flickerAlpha = 0f; delay(20)
        flickerAlpha = 1f; glitchPower = 0.3f; delay(50)
        glitchPower = 0f

        // ── SETTLE — decaying flickers (1.5s-2.0s) ──
        delay(100)
        flickerAlpha = 0.5f; delay(30); flickerAlpha = 1f; delay(120)
        flickerAlpha = 0.7f; delay(20); flickerAlpha = 1f; delay(180)
        flickerAlpha = 0.85f; delay(15); flickerAlpha = 1f

        // ── CLEAN HOLD (2.0s-2.5s) ──
        delay(500)

        // ── FADE OUT (2.5s-3.0s) ──
        fadeOut.animateTo(0f, tween(500))

        // Cleanup + navigate
        mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    // Continuous glitch tick for slice animation
    val inf = rememberInfiniteTransition(label = "G")
    val glitchTick by inf.animateFloat(
        0f, 10f,
        infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "t",
    )

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        logoBitmap?.let { bitmap ->
            if (flickerAlpha > 0f) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = flickerAlpha * fadeOut.value }
                ) {
                    val w = size.width; val h = size.height
                    // Scale logo to fit ~45% of screen width (it's wide)
                    val targetW = w * 0.45f
                    val sc = targetW / bitmap.width
                    val dW = (bitmap.width * sc).toInt()
                    val dH = (bitmap.height * sc).toInt()
                    val oX = ((w - dW) / 2f).toInt()
                    val oY = ((h - dH) / 2f).toInt()

                    val effectiveGlitch = glitchPower * (glitchTick / 10f).coerceIn(0.3f, 1f)

                    if (effectiveGlitch < 0.1f) {
                        // Clean frame
                        drawImage(bitmap, dstOffset = IntOffset(oX, oY), dstSize = IntSize(dW, dH))
                    } else {
                        // Glitch frame
                        drawGlitchedImage(bitmap, oX, oY, dW, dH, w, effectiveGlitch)
                    }

                    // Scanlines overlay — subtle CRT feel
                    if (glitchPower > 0.2f) {
                        drawScanlines(w, h, glitchPower * 0.04f)
                    }
                }
            }
        }
    }
}

/** Draw the image with horizontal slice displacement + chromatic artifacts */
private fun DrawScope.drawGlitchedImage(
    bitmap: ImageBitmap,
    oX: Int, oY: Int, dW: Int, dH: Int,
    canvasW: Float,
    intensity: Float,
) {
    val slices = 20
    val sliceH = dH.toFloat() / slices
    val glitchColors = listOf(Color(0xFF00FFFF), Color(0xFFFF0055), Color(0xFF00FF66), Color(0xFFFF00FF))

    // RGB split ghost (behind main image)
    if (intensity > 0.3f) {
        val splitX = intensity * 8f
        // Red channel ghost
        drawImage(
            bitmap,
            dstOffset = IntOffset((oX + splitX).toInt(), oY),
            dstSize = IntSize(dW, dH),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                Color.Red.copy(alpha = intensity * 0.3f),
                BlendMode.SrcAtop,
            ),
        )
        // Cyan channel ghost
        drawImage(
            bitmap,
            dstOffset = IntOffset((oX - splitX).toInt(), oY),
            dstSize = IntSize(dW, dH),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                Color.Cyan.copy(alpha = intensity * 0.25f),
                BlendMode.SrcAtop,
            ),
        )
    }

    // Sliced main image
    for (i in 0 until slices) {
        val shift = if (Random.nextFloat() < intensity * 0.6f) {
            (Random.nextFloat() - 0.5f) * intensity * 40f
        } else 0f

        val top = oY + i * sliceH
        val bot = oY + (i + 1) * sliceH

        clipRect(0f, top, canvasW, bot) {
            translate(left = shift) {
                drawImage(bitmap, dstOffset = IntOffset(oX, oY), dstSize = IntSize(dW, dH))
            }

            // Color artifact on random slices
            if (Random.nextFloat() < intensity * 0.15f) {
                drawRect(
                    glitchColors[Random.nextInt(glitchColors.size)].copy(alpha = intensity * 0.4f),
                    Offset(oX.toFloat() + shift, top),
                    Size(dW.toFloat(), sliceH),
                    blendMode = BlendMode.SrcAtop,
                )
            }
        }
    }

    // Screen tear lines
    if (intensity > 0.5f) {
        val tearCount = (intensity * 4).toInt().coerceAtLeast(1)
        repeat(tearCount) {
            val tearY = oY + Random.nextFloat() * dH
            val tearShift = (Random.nextFloat() - 0.5f) * intensity * 60f
            drawRect(
                Color.White.copy(alpha = intensity * 0.15f),
                Offset(oX.toFloat() + tearShift, tearY),
                Size(dW.toFloat(), 2f),
            )
        }
    }
}

/** Subtle horizontal scanlines — CRT monitor feel */
private fun DrawScope.drawScanlines(w: Float, h: Float, alpha: Float) {
    var y = 0f
    while (y < h) {
        drawLine(Color.Black.copy(alpha = alpha), Offset(0f, y), Offset(w, y), 1f)
        y += 3f
    }
}
