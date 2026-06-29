package lux.vexium.app.core.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder

/**
 * Vexium logo loaded from SVG assets via Coil.
 * Automatically picks dark/light variant.
 *
 * Assets:
 *   assets/logo/dark_text.svg
 *   assets/logo/light_text.svg
 */
@Composable
fun VexiumLogo(
    modifier: Modifier = Modifier,
    isDark: Boolean = true, // Dark theme only
    width: Dp = 300.dp,
    height: Dp = 120.dp,
) {
    val svgFile = if (isDark) "logo/dark_text.svg" else "logo/light_text.svg"

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/$svgFile")
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = "Vexium",
        modifier = modifier
            .width(width)
            .height(height),
        contentScale = ContentScale.Fit,
    )
}
