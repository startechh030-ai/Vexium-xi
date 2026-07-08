package lux.obris.app.feature.splash.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Splash — shattered "O" mark with glitch effects.
 * ~2.5s punchy stinger then fade out.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val glitchIntensity = remember { Animatable(0f) }
    val fadeOut = remember { Animatable(1f) }
    val glitchSeed = remember { Animatable(0f) }
    val flicker = remember { Animatable(1f) }
    val scale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        delay(150)
        logoAlpha.animateTo(1f, tween(300))
        scale.animateTo(1f, tween(300, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)))
        delay(180)

        // Glitch burst 1
        glitchIntensity.animateTo(0.55f, tween(60))
        glitchSeed.animateTo(1f, tween(60))
        glitchIntensity.animateTo(0f, tween(70))
        delay(110)

        // Glitch burst 2
        glitchIntensity.animateTo(0.8f, tween(50))
        glitchSeed.animateTo(2f, tween(50))
        glitchIntensity.animateTo(0.15f, tween(40))
        glitchSeed.animateTo(3f, tween(40))
        glitchIntensity.animateTo(0.9f, tween(45))
        glitchIntensity.animateTo(0f, tween(90))
        delay(130)

        // Glitch burst 3 — heavy
        flicker.animateTo(0.12f, tween(20))
        glitchIntensity.animateTo(1f, tween(35))
        glitchSeed.animateTo(4f, tween(35))
        flicker.animateTo(1f, tween(30))
        delay(45)
        glitchIntensity.animateTo(0.25f, tween(25))
        glitchSeed.animateTo(5f, tween(25))
        flicker.animateTo(0.3f, tween(25))
        glitchIntensity.animateTo(1f, tween(35))
        glitchSeed.animateTo(6f, tween(35))
        flicker.animateTo(1f, tween(40))
        glitchIntensity.animateTo(0.4f, tween(30))
        glitchSeed.animateTo(7f, tween(30))
        glitchIntensity.animateTo(0f, tween(110))

        // Settle flicker
        repeat(3) { i ->
            flicker.animateTo(0.45f, tween(22))
            flicker.animateTo(1f, tween(35 + i * 20))
        }

        // Snap
        scale.animateTo(1.1f, tween(90, easing = CubicBezierEasing(0.3f, 0f, 0.2f, 1f)))
        scale.animateTo(1f, tween(140, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)))

        delay(450)
        fadeOut.animateTo(0f, tween(400))
        onSplashFinished()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            val cx = w / 2f; val cy = h / 2f
            val alpha = logoAlpha.value * fadeOut.value * flicker.value
            val glitch = glitchIntensity.value
            val seed = glitchSeed.value

            drawVignette(cx, cy, w, h)
            drawScanlines(w, h, alpha = 0.05f + glitch * 0.05f)
            if (alpha <= 0f) return@Canvas

            val outerR = minOf(w, h) * 0.34f * scale.value
            drawShatteredO(cx, cy, outerR, alpha, glitch, seed)
            drawBrandLabel(cx, cy, outerR, alpha, glitch, seed)
            if (glitch > 0.15f) drawDigitalNoise(w, h, glitch, seed)
        }
    }
}

// ── Cut geometry for the fracture line ──
private data class CutGeometry(val p0: Offset, val p1: Offset, val p2: Offset, val p3: Offset, val p4: Offset, val p5: Offset)

private fun computeCutGeometry(cx: Float, cy: Float, outerR: Float, jitter: Float, seed: Int): CutGeometry {
    val rng = Random(seed)
    fun j() = (rng.nextFloat() - 0.5f) * jitter
    fun pt(fx: Float, fy: Float) = Offset(cx + fx * outerR + j(), cy + fy * outerR + j())
    return CutGeometry(pt(0.95f, -1.45f), pt(0.60f, -0.85f), pt(0.30f, -0.25f), pt(0.02f, 0.05f), pt(-0.30f, 0.30f), pt(-0.95f, 1.05f))
}

private fun buildAnnulus(cx: Float, cy: Float, outerR: Float, innerR: Float): Path {
    val outer = Path().apply { addOval(Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR)) }
    val inner = Path().apply { addOval(Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR)) }
    return Path().apply { op(outer, inner, PathOperation.Difference) }
}

private fun buildDividerRegions(cx: Float, cy: Float, outerR: Float, geo: CutGeometry): Pair<Path, Path> {
    val m = outerR * 25f
    val top = Path().apply {
        moveTo(cx - m, cy - m); lineTo(cx + m, cy - m)
        lineTo(geo.p0.x, geo.p0.y); lineTo(geo.p1.x, geo.p1.y); lineTo(geo.p2.x, geo.p2.y)
        lineTo(geo.p3.x, geo.p3.y); lineTo(geo.p4.x, geo.p4.y); lineTo(geo.p5.x, geo.p5.y)
        lineTo(cx - m, cy + m); close()
    }
    val box = Path().apply { addRect(Rect(cx - m, cy - m, cx + m, cy + m)) }
    val bottom = Path().apply { op(box, top, PathOperation.Difference) }
    return top to bottom
}

private fun buildShard(geo: CutGeometry, width: Float): Path {
    val dx = geo.p0.x - geo.p1.x; val dy = geo.p0.y - geo.p1.y
    val len = max(sqrt(dx * dx + dy * dy), 0.001f)
    val nx = -dy / len; val ny = dx / len
    return Path().apply {
        moveTo(geo.p1.x + nx * width, geo.p1.y + ny * width)
        lineTo(geo.p0.x, geo.p0.y)
        lineTo(geo.p1.x - nx * width, geo.p1.y - ny * width)
        close()
    }
}

private fun DrawScope.drawShatteredO(cx: Float, cy: Float, outerR: Float, alpha: Float, glitch: Float, seed: Float) {
    val innerR = outerR * 0.58f
    val strokeGlow = outerR * 0.03f
    val neon = Color(0xFFFF8C00)

    val restingGap = outerR * 0.045f
    val gap = restingGap + outerR * 0.16f * glitch
    val jitterAmt = outerR * 0.05f * glitch

    val geo = computeCutGeometry(cx, cy, outerR, jitterAmt, seed.toInt() * 97 + 13)
    val annulus = buildAnnulus(cx, cy, outerR, innerR)
    val (topRegion, bottomRegion) = buildDividerRegions(cx, cy, outerR, geo)
    val shard = buildShard(geo, outerR * 0.045f)

    val topBase = Path().apply { op(annulus, topRegion, PathOperation.Intersect) }
    val topPiece = Path().apply { op(topBase, shard, PathOperation.Union) }
    val bottomPiece = Path().apply { op(annulus, bottomRegion, PathOperation.Intersect) }

    topPiece.translate(Offset(-gap * 0.65f, -gap * 0.65f))
    bottomPiece.translate(Offset(gap * 0.7f, gap * 0.5f))

    // Glow
    val glow = outerR * 2.6f
    drawCircle(
        brush = Brush.radialGradient(listOf(neon.copy(alpha = alpha * (0.12f + glitch * 0.14f)), Color.Transparent), Offset(cx, cy), glow.coerceAtLeast(10f)),
        radius = glow.coerceAtLeast(10f), center = Offset(cx, cy),
    )

    // RGB ghosts
    if (glitch > 0.08f) {
        val rng = Random(seed.toInt() * 31 + 5)
        val off = glitch * outerR * 0.06f
        val ro = Offset((rng.nextFloat() - 0.5f) * off * 2f, (rng.nextFloat() - 0.5f) * off)
        val co = Offset((rng.nextFloat() - 0.5f) * off * 2f, (rng.nextFloat() - 0.5f) * off)

        listOf(topPiece, bottomPiece).forEach { p ->
            val pr = Path().apply { addPath(p); translate(ro) }
            val pc = Path().apply { addPath(p); translate(co) }
            drawPath(pr, Color.Red.copy(alpha = alpha * glitch * 0.5f), style = Fill)
            drawPath(pc, Color.Cyan.copy(alpha = alpha * glitch * 0.4f), style = Fill)
        }

        val sliceCount = (glitch * 6).toInt().coerceAtLeast(1)
        repeat(sliceCount) {
            val sliceY = cy - outerR * 1.3f + outerR * 2.6f * rng.nextFloat()
            val sliceH = outerR * 0.09f * rng.nextFloat() + 1.5f
            val sliceDx = (rng.nextFloat() - 0.5f) * outerR * 0.5f * glitch
            val tearColor = listOf(Color.White, Color.Cyan, Color.Red, neon)[rng.nextInt(4)]
            drawRect(tearColor.copy(alpha = alpha * 0.45f * glitch), Offset(cx - outerR - strokeGlow + sliceDx, sliceY), Size(outerR * 2f + strokeGlow * 2f, sliceH))
        }
    }

    // Chrome fill
    val chrome = Brush.linearGradient(
        listOf(Color(0xFFF3F3F5), Color(0xFFC9C9D1), Color(0xFF9C9CA6), Color(0xFFFFFFFF), Color(0xFFA6A6AF)),
        Offset(cx - outerR, cy - outerR), Offset(cx + outerR, cy + outerR),
    )

    listOf(topPiece, bottomPiece).forEach { piece ->
        drawPath(piece, brush = chrome, style = Fill, alpha = alpha)
        drawPath(piece, neon.copy(alpha = alpha * (0.5f + glitch * 0.5f)), style = Stroke(strokeGlow))
    }
}

private fun DrawScope.drawBrandLabel(cx: Float, cy: Float, outerR: Float, alpha: Float, glitch: Float, seed: Float) {
    val textSizePx = outerR * 0.32f
    val baseY = cy + outerR * 1.7f
    val rng = Random(seed.toInt() * 53 + 7)
    val jitter = if (glitch > 0.2f) (rng.nextFloat() - 0.5f) * glitch * outerR * 0.06f else 0f

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = textSizePx
            textAlign = android.graphics.Paint.Align.CENTER
            letterSpacing = 0.18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }

        if (glitch > 0.15f) {
            paint.color = android.graphics.Color.argb((alpha * glitch * 140).toInt().coerceIn(0, 255), 255, 0, 0)
            drawText("O B R I S", cx + jitter * 1.5f, baseY, paint)
            paint.color = android.graphics.Color.argb((alpha * glitch * 120).toInt().coerceIn(0, 255), 0, 255, 255)
            drawText("O B R I S", cx - jitter * 1.5f, baseY, paint)
        }

        paint.color = android.graphics.Color.argb((alpha * 255).toInt().coerceIn(0, 255), 255, 140, 0)
        drawText("O B R I S", cx + jitter, baseY, paint)
    }
}

private fun DrawScope.drawScanlines(w: Float, h: Float, alpha: Float) {
    if (alpha <= 0f) return
    var y = 0f
    while (y < h) {
        drawLine(Color.White.copy(alpha = alpha), Offset(0f, y), Offset(w, y), 1f)
        y += 4f
    }
}

private fun DrawScope.drawVignette(cx: Float, cy: Float, w: Float, h: Float) {
    drawRect(
        Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)), Offset(cx, cy), max(w, h) * 0.75f),
        size = Size(w, h),
    )
}

private fun DrawScope.drawDigitalNoise(w: Float, h: Float, glitch: Float, seed: Float) {
    val rng = Random(seed.toInt() * 71 + 3)
    repeat((glitch * 90).toInt()) {
        drawRect(
            Color.White.copy(alpha = rng.nextFloat() * 0.2f * glitch),
            Offset(rng.nextFloat() * w, rng.nextFloat() * h),
            Size(rng.nextFloat() * 2.5f + 0.5f, 1.5f),
        )
    }
}
