package lux.vexium.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders the "Vexium" logo text with gradient effects matching the SVG assets.
 * Automatically adapts to dark/light theme.
 */
@Composable
fun VexiumLogo(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
) {
    val textMeasurer = rememberTextMeasurer()

    // ── Dark theme: Silver → Black metallic gradient ──
    val darkGradientColors = listOf(
        Color(0xFFF0F0F0),
        Color(0xFFDCDCDC),
        Color(0xFFC8C8C8),
        Color(0xFFA8A8A8),
        Color(0xFF808080),
        Color(0xFF4A4A4A),
        Color(0xFF1E1E1E),
        Color(0xFF080808),
        Color(0xFF000000),
    )

    // ── Light theme: Ice blue → Deep blue gradient ──
    val lightGradientColors = listOf(
        Color(0xFFD4F8FF),
        Color(0xFFB2F0FF),
        Color(0xFF8FE5FF),
        Color(0xFF68D4FF),
        Color(0xFF3DB8FF),
        Color(0xFF1A94E8),
        Color(0xFF0C6ECF),
        Color(0xFF0850A8),
        Color(0xFF063D8A),
    )

    // ── Metallic shine overlay (top bright → transparent) ──
    val shineColors = if (isDark) {
        listOf(
            Color.White.copy(alpha = 0.60f),
            Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.02f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.15f),
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.42f),
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.04f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.10f),
        )
    }

    val gradientColors = if (isDark) darkGradientColors else lightGradientColors

    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.50f)
    } else {
        Color(0xFF042D60).copy(alpha = 0.35f)
    }

    val fontSize = 72.sp

    val baseStyle = TextStyle(
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        letterSpacing = (-2).sp,
    )

    Canvas(
        modifier = modifier
            .width(320.dp)
            .height(90.dp),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Measure text to center it
        val textLayoutResult = textMeasurer.measure(
            text = "Vexium",
            style = baseStyle,
        )
        val textWidth = textLayoutResult.size.width.toFloat()
        val textHeight = textLayoutResult.size.height.toFloat()
        val offsetX = (canvasWidth - textWidth) / 2f
        val offsetY = (canvasHeight - textHeight) / 2f
        val topLeft = Offset(offsetX, offsetY)

        // Layer 1: Shadow
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "Vexium",
                style = baseStyle.copy(
                    shadow = Shadow(
                        color = shadowColor,
                        offset = Offset(0f, 4f),
                        blurRadius = 10f,
                    ),
                    color = Color.Transparent,
                ),
            ),
            topLeft = topLeft,
        )

        // Layer 2: Main gradient text
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "Vexium",
                style = baseStyle.copy(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = offsetY,
                        endY = offsetY + textHeight,
                    ),
                ),
            ),
            topLeft = topLeft,
        )

        // Layer 3: Metallic shine overlay
        drawText(
            textLayoutResult = textMeasurer.measure(
                text = "Vexium",
                style = baseStyle.copy(
                    brush = Brush.verticalGradient(
                        colors = shineColors,
                        startY = offsetY,
                        endY = offsetY + textHeight,
                    ),
                ),
            ),
            topLeft = topLeft,
        )
    }
}
