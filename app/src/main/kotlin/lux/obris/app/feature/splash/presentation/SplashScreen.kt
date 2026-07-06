package lux.obris.app.feature.splash.presentation

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Splash screen — plays splash.mp4 full screen.
 * Waits for video to be ready before showing to avoid layout glitch.
 */
@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    val context = LocalContext.current
    var videoReady by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${lux.obris.app.R.raw.splash}")
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    // Show video only when it starts rendering
                    if (playbackState == Player.STATE_READY) {
                        videoReady = true
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        onSplashFinished()
                    }
                }
            })

            prepare()
        }
    }

    // Safety timeout — if video fails, still navigate after 6s
    LaunchedEffect(Unit) {
        delay(6000)
        if (!videoReady) onSplashFinished()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Only show PlayerView when video is ready — prevents layout glitch
        if (videoReady) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
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
