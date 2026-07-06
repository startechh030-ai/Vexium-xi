package com.vexium.splash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Add to res/font: Download a monospace font like "JetBrains Mono" or use system default
// For best results, add jetbrains_mono_regular.ttf to res/font/

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CyberpunkSplashScreen(
                onSplashComplete = {
                    // Navigate to MainActivity
                    // startActivity(Intent(this, MainActivity::class.java))
                    // finish()
                }
            )
        }
    }
}

@Composable
fun CyberpunkSplashScreen(
    modifier: Modifier = Modifier,
    onSplashComplete: () -> Unit = {}
) {
    val cyan = Color(0xFF00FFFF)
    val magenta = Color(0xFFFF00FF)
    val darkBg = Color(0xFF050508)
    val white = Color.White

    // Animation states
    var statusVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var percentVisible by remember { mutableStateOf(false) }
    var rewindVisible by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf(0f) }
    var subtitleText by remember { mutableStateOf("") }
    var glitchIntensity by remember { mutableStateOf(0f) }
    var flashAlpha by remember { mutableStateOf(0f) }
    var flashColor by remember { mutableStateOf(cyan) }

    // Glitch offset animations
    val glitchCyanOffsetX by animateFloatAsState(
        targetValue = if (glitchIntensity > 0.7f) Random.nextFloat() * 12f - 6f else 0f,
        animationSpec = tween(50),
        label = "glitchCyan"
    )
    val glitchMagentaOffsetX by animateFloatAsState(
        targetValue = if (glitchIntensity > 0.7f) Random.nextFloat() * 12f - 6f else 0f,
        animationSpec = tween(50),
        label = "glitchMagenta"
    )

    // Particle system
    val particles = remember {
        List(60) {
            ParticleData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.002f,
                vy = (Random.nextFloat() - 0.5f) * 0.002f,
                size = Random.nextFloat() * 3f + 1f,
                color = if (Random.nextBoolean()) cyan else magenta
            )
        }
    }

    // Glitch lines
    var glitchLines by remember { mutableStateOf(listOf<GlitchLine>()) }

    // Data stream offset
    var dataStreamOffset by remember { mutableFloatStateOf(0f) }

    // Cursor blink
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Cursor blink
        launch {
            while (true) {
                delay(500)
                cursorVisible = !cursorVisible
            }
        }

        // Data stream scroll
        launch {
            while (true) {
                delay(16)
                dataStreamOffset -= 1f
                if (dataStreamOffset < -200f) dataStreamOffset = 0f
            }
        }

        // Random glitch triggers
        launch {
            while (true) {
                delay(200)
                glitchIntensity = Random.nextFloat()

                // Random glitch lines
                if (Random.nextFloat() > 0.8f) {
                    val newLine = GlitchLine(
                        y = Random.nextFloat(),
                        height = Random.nextFloat() * 4f + 2f,
                        color = if (Random.nextBoolean()) cyan else magenta,
                        duration = Random.nextInt(100, 300)
                    )
                    glitchLines = glitchLines + newLine
                    launch {
                        delay(newLine.duration.toLong())
                        glitchLines = glitchLines - newLine
                    }
                }
            }
        }

        // Sequence
        delay(300)
        statusVisible = true

        delay(500)
        subtitleVisible = true

        // Typewriter effect
        val fullSubtitle = "CYBERPUNK PROTOCOL"
        fullSubtitle.forEach { char ->
            delay(80)
            subtitleText += char
        }

        delay(400)
        percentVisible = true

        // Progress bar
        while (progressValue < 100f) {
            delay(50)
            progressValue += Random.nextFloat() * 3f
            if (progressValue > 100f) progressValue = 100f
        }

        // Rewind burst
        delay(200)
        rewindVisible = true
        flashColor = magenta
        flashAlpha = 0.3f
        glitchIntensity = 1f

        delay(150)
        flashAlpha = 0f
        flashColor = cyan

        delay(800)
        onSplashComplete()
    }

    // Flash animation
    val animatedFlashAlpha by animateFloatAsState(
        targetValue = flashAlpha,
        animationSpec = tween(100),
        label = "flash"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Background grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 60f
            val width = size.width
            val height = size.height

            // Horizontal lines
            var y = 0f
            while (y < height) {
                drawLine(
                    color = magenta.copy(alpha = 0.03f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }

            // Vertical lines
            var x = 0f
            while (x < width) {
                drawLine(
                    color = cyan.copy(alpha = 0.03f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSize
            }
        }

        // Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                p.x += p.vx
                p.y += p.vy
                if (p.x < 0f || p.x > 1f) p.vx *= -1f
                if (p.y < 0f || p.y > 1f) p.vy *= -1f

                drawRect(
                    color = p.color.copy(alpha = 0.4f),
                    topLeft = Offset(p.x * size.width, p.y * size.height),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size)
                )
            }
        }

        // Scanlines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineHeight = 4f
            var scanY = 0f
            while (scanY < size.height) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(0f, scanY + 2f),
                    size = androidx.compose.ui.geometry.Size(size.width, 2f)
                )
                scanY += lineHeight
            }
        }

        // Vignette
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    ),
                    center = center,
                    radius = size.width * 0.8f
                )
            )
        }

        // Glitch lines
        glitchLines.forEach { line ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(line.height.dp)
                    .offset(y = (line.y * 100).dp)
                    .background(line.color.copy(alpha = 0.4f))
            )
        }

        // Flash overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(animatedFlashAlpha)
                .background(flashColor)
                .graphicsLayer { blendMode = BlendMode.Overlay }
        )

        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status bar
            Text(
                text = "SYSTEM INITIALIZING // SFX-540234 LOADED",
                color = cyan,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(if (statusVisible) 1f else 0f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Glitch Logo
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                // Magenta layer
                Text(
                    text = "VEXIUM",
                    color = magenta.copy(alpha = 0.7f),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = glitchMagentaOffsetX.dp)
                )

                // Cyan layer
                Text(
                    text = "VEXIUM",
                    color = cyan.copy(alpha = 0.7f),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = glitchCyanOffsetX.dp)
                )

                // Main white layer
                Text(
                    text = "VEXIUM",
                    color = white,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 20f
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle with typewriter
            Text(
                text = subtitleText + if (cursorVisible && subtitleVisible) "_" else "",
                color = magenta,
                fontSize = 14.sp,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(if (subtitleVisible) 1f else 0f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressValue / 100f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(cyan, magenta)
                            )
                        )
                        .graphicsLayer {
                            shadowElevation = 10f
                            spotShadowColor = cyan
                        }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage
            Text(
                text = "${progressValue.toInt()}%",
                color = cyan,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(if (percentVisible) 1f else 0f)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Rewind icon
            Text(
                text = "◄◄◄ REWIND ◄◄◄",
                color = magenta,
                fontSize = 24.sp,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(if (rewindVisible) 1f else 0f)
            )
        }

        // Corner decorations
        CornerDecoration(
            modifier = Modifier.align(Alignment.TopStart),
            top = true, left = true, color = cyan
        )
        CornerDecoration(
            modifier = Modifier.align(Alignment.TopEnd),
            top = true, left = false, color = magenta
        )
        CornerDecoration(
            modifier = Modifier.align(Alignment.BottomStart),
            top = false, left = true, color = magenta
        )
        CornerDecoration(
            modifier = Modifier.align(Alignment.BottomEnd),
            top = false, left = false, color = cyan
        )

        // Data stream
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
                .height(20.dp)
                .clip(RectangleShape)
        ) {
            Text(
                text = "0x7F3A // GLITCH_SFX_540234 // REWIND_PROTOCOL_ACTIVE // MEMORY_CORRUPTION_DETECTED // 0x9B2C // ",
                color = cyan.copy(alpha = 0.3f),
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                maxLines = 1,
                modifier = Modifier.offset(x = dataStreamOffset.dp)
            )
        }
    }
}

@Composable
fun CornerDecoration(
    modifier: Modifier = Modifier,
    top: Boolean,
    left: Boolean,
    color: Color
) {
    Box(
        modifier = modifier
            .padding(20.dp)
            .size(40.dp)
            .drawWithContent {
                drawContent()
                val strokeWidth = 2.dp.toPx()

                if (top) {
                    drawLine(
                        color = color,
                        start = if (left) Offset(0f, 0f) else Offset(size.width, 0f),
                        end = if (left) Offset(size.width * 0.6f, 0f) else Offset(size.width * 0.4f, 0f),
                        strokeWidth = strokeWidth
                    )
                } else {
                    drawLine(
                        color = color,
                        start = if (left) Offset(0f, size.height) else Offset(size.width, size.height),
                        end = if (left) Offset(size.width * 0.6f, size.height) else Offset(size.width * 0.4f, size.height),
                        strokeWidth = strokeWidth
                    )
                }

                if (left) {
                    drawLine(
                        color = color,
                        start = if (top) Offset(0f, 0f) else Offset(0f, size.height),
                        end = if (top) Offset(0f, size.height * 0.6f) else Offset(0f, size.height * 0.4f),
                        strokeWidth = strokeWidth
                    )
                } else {
                    drawLine(
                        color = color,
                        start = if (top) Offset(size.width, 0f) else Offset(size.width, size.height),
                        end = if (top) Offset(size.width, size.height * 0.6f) else Offset(size.width, size.height * 0.4f),
                        strokeWidth = strokeWidth
                    )
                }
            }
    )
}

// Data classes
data class ParticleData(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color
)

data class GlitchLine(
    val y: Float,
    val height: Float,
    val color: Color,
    val duration: Int
)
