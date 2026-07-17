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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
 * Splash screen — 3 phases:
 *   Phase 0: Logo + glitch + sound (2s, synced)
 *   Phase 1: Curtain reveal transition (0.6s)
 *   Phase 2: Navigate
 *
 * Sound: 50% volume, distant echo feel.
 * Curtain: screen splits from center, left half slides left, right slides right.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var phase by remember { mutableIntStateOf(0) } // 0=glitch, 1=curtain, 2=done

    // Curtain animation: 0 = closed (full screen), 1 = open (halves off screen)
    val curtainProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Create MediaPlayer FIRST — synchronous, no coroutine delay
        val player = try {
            MediaPlayer.create(context, lux.obris.app.R.raw.glitch)?.apply {
                // 50% volume, distant feel (left=0.5, right=0.5)
                setVolume(0.5f, 0.5f)
                setOnCompletionListener { it.release() }
            }
        } catch (_: Exception) { null }
        mediaPlayer = player

        // Load logo on IO thread in parallel
        launch(Dispatchers.IO) {
            try {
                context.assets.open("splash/obris.png").use { stream ->
                    logoBitmap = BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {}
        }

        // Start sound NOW — same frame as visual
        player?.start()

        // Wait for sound to finish (2 seconds)
        // Use the actual sound duration if available, else default 2s
        val soundDuration = player?.duration?.toLong() ?: 2000L
        delay(soundDuration)

        // Phase 1: Curtain reveal
        phase = 1
        curtainProgress.animateTo(1f, tween(600, easing = LinearEasing))

        // Phase 2: Navigate
        phase = 2
        mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        }
    }

    // Glitch animation — 1s forward, 1s reverse
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
        when (phase) {
            // ── Phase 0: Logo with glitch ──
            0 -> {
                logoBitmap?.let { bitmap ->
                    GlitchLogo(bitmap = bitmap, glitchTick = glitchTick, alpha = 1f)
                }
            }

            // ── Phase 1: Curtain reveal — splits from center ──
            1 -> {
                val progress = curtainProgress.value

                Row(modifier = Modifier.fillMaxSize()) {
                    // Left curtain — slides left
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                            .graphicsLayer {
                                translationX = -size.width * progress
                            }
                            .background(Color.Black),
                    ) {
                        // Draw the left half of the logo (clipped)
                        logoBitmap?.let { bitmap ->
                            GlitchLogo(bitmap = bitmap, glitchTick = 0f, alpha = 1f - progress * 0.5f)
                        }
                    }

                    // Right curtain — slides right
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationX = size.width * progress
                            }
                            .background(Color.Black),
                    ) {
                        logoBitmap?.let { bitmap ->
                            GlitchLogo(bitmap = bitmap, glitchTick = 0f, alpha = 1f - progress * 0.5f)
                        }
                    }
                }
            }
        }
    }
}

/** Draws the logo bitmap with cyberpunk glitch — slices + chromatic artifacts */
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

        val targetH = canvasH * 0.35f
        val scale = targetH / bitmap.height.toFloat()
        val drawW = (bitmap.width * scale).toInt()
        val drawH = (bitmap.height * scale).toInt()
        val offsetX = ((canvasW - drawW) / 2f).toInt()
        val offsetY = ((canvasH - drawH) / 2f).toInt()

        val isGlitchActive = glitchStep in 2..6
        val intensity = if (isGlitchActive) (glitchStep / 10f) else 0f

        if (!isGlitchActive) {
            drawImage(bitmap, dstOffset = IntOffset(offsetX, offsetY), dstSize = IntSize(drawW, drawH))
        } else {
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
