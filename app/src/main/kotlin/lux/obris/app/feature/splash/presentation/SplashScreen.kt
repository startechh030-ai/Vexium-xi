package lux.obris.app.feature.splash.presentation

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Obris splash screen — two phases in one composable:
 *
 * Phase 1 (0-2s): Axiom studio logo
 *   - Logo appears centered, slides left ~30%, text "AXIOM" fades up
 *   - Fades out
 *
 * Phase 2 (2-7s): Obris video splash
 *   - Plays the best-fit .mp4 from res/raw
 *   - Full screen, no crop, fits device
 *   - When done → onSplashFinished()
 */
@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    val context = LocalContext.current

    // ── State machine ──
    // 0 = Axiom intro, 1 = Axiom fade out, 2 = Video playing, 3 = Done
    var phase by remember { mutableIntStateOf(0) }
    var showAxiom by remember { mutableStateOf(true) }
    var showVideo by remember { mutableStateOf(false) }

    // ── Axiom animations ──
    val logoOffsetX = remember { Animatable(0f) }    // 0 = center, -1 = left
    val textAlpha = remember { Animatable(0f) }
    val axiomAlpha = remember { Animatable(1f) }

    // ── Pick best video for device aspect ratio ──
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val screenHeight = config.screenHeightDp
    val videoRes = remember {
        pickBestVideo(screenWidth.toFloat() / screenHeight.toFloat())
    }

    // ── ExoPlayer ──
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$videoRes")
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = false // Don't play until phase 2
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f // Silent
            prepare()
        }
    }

    // Listen for video end
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    phase = 3
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // ── Animation timeline ──
    LaunchedEffect(Unit) {
        // Phase 0: Axiom intro (2 seconds)
        delay(300)

        // Logo slides left ~30%
        logoOffsetX.animateTo(-0.3f, tween(500, easing = LinearEasing))

        // Text fades in from below
        textAlpha.animateTo(1f, tween(400))

        // Hold for a moment
        delay(600)

        // Phase 1: Fade out Axiom
        phase = 1
        axiomAlpha.animateTo(0f, tween(300))

        // Phase 2: Start video
        showAxiom = false
        showVideo = true
        phase = 2
        exoPlayer.play()
    }

    // ── When video finishes (phase 3) → navigate ──
    LaunchedEffect(phase) {
        if (phase == 3) {
            delay(200) // Brief pause
            onSplashFinished()
        }
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // ── Phase 1: Axiom Studio Logo ──
        if (showAxiom) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (logoOffsetX.value * 120.dp.toPx()).toInt(),
                            y = 0,
                        )
                    },
                ) {
                    // Axiom diamond logo
                    Canvas(
                        modifier = Modifier
                            .size(48.dp)
                            .offset { IntOffset(0, 0) },
                    ) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f
                        val s = w * 0.38f

                        // Diamond shape
                        val diamond = Path().apply {
                            moveTo(cx, cy - s)       // Top
                            lineTo(cx + s, cy)        // Right
                            lineTo(cx, cy + s)        // Bottom
                            lineTo(cx - s, cy)        // Left
                            close()
                        }
                        drawPath(
                            diamond,
                            color = Color.White.copy(alpha = axiomAlpha.value),
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )

                        // Inner "A" mark
                        val innerPath = Path().apply {
                            moveTo(cx - s * 0.3f, cy + s * 0.15f)
                            lineTo(cx, cy - s * 0.35f)
                            lineTo(cx + s * 0.3f, cy + s * 0.15f)
                        }
                        drawPath(
                            innerPath,
                            color = Color.White.copy(alpha = axiomAlpha.value * 0.8f),
                            style = Stroke(width = 1.8f, cap = StrokeCap.Round),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // "AXIOM" text
                    Text(
                        text = "AXIOM",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = textAlpha.value * axiomAlpha.value),
                            letterSpacing = 8.sp,
                        ),
                    )
                }
            }
        }

        // ── Phase 2: Obris Video ──
        AnimatedVisibility(
            visible = showVideo && phase >= 2,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300)),
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Pick the best video resolution for the device aspect ratio.
 * Videos in res/raw: splash_16x9, splash_4x3, splash_5x4, splash_1x1
 */
private fun pickBestVideo(aspectRatio: Float): Int {
    return when {
        aspectRatio >= 1.6f -> lux.obris.app.R.raw.splash_16x9    // Wide screens (16:9, 18:9)
        aspectRatio >= 1.2f -> lux.obris.app.R.raw.splash_4x3     // Standard (4:3)
        aspectRatio >= 1.0f -> lux.obris.app.R.raw.splash_5x4     // Square-ish (5:4)
        else -> lux.obris.app.R.raw.splash_1x1                     // Fallback square
    }
}
