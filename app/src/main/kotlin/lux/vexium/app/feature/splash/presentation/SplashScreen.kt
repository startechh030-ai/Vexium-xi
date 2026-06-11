package lux.vexium.app.feature.splash.presentation

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import lux.vexium.app.R
import lux.vexium.app.core.theme.DarkNavy

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    val context = LocalContext.current

    // Track video completion & crossfade
    var isVideoFinished by remember { mutableStateOf(false) }
    var showVideo by remember { mutableStateOf(true) }

    // Build ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}")
            setMediaItem(MediaItem.fromUri(videoUri))
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f // silent splash

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        isVideoFinished = true
                    }
                }
            })

            prepare()
        }
    }

    // When video finishes → crossfade out → navigate
    LaunchedEffect(isVideoFinished) {
        if (isVideoFinished) {
            showVideo = false       // trigger fade-out
            delay(600)              // wait for crossfade animation
            onSplashFinished()      // navigate to next screen
        }
    }

    // Clean up player when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy),
        contentAlignment = Alignment.Center,
    ) {
        // Video with crossfade
        AnimatedVisibility(
            visible = showVideo,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(600)),
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false                              // no play/pause controls
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // fit video, keep aspect ratio
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        // Dark navy background behind the video (fills borders)
                        setBackgroundColor(android.graphics.Color.parseColor("#0A0E1A"))
                        setShutterBackgroundColor(android.graphics.Color.parseColor("#0A0E1A"))
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
