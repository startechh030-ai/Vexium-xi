package lux.obris.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Obris dark scheme — orange/cyan/purple on deep dark ──
private val ObrisDarkScheme = darkColorScheme(
    primary = ObrisPrimary,
    onPrimary = ObrisOnPrimary,
    primaryContainer = ObrisPrimaryContainer,
    onPrimaryContainer = ObrisOnPrimaryContainer,
    secondary = ObrisSecondary,
    onSecondary = ObrisOnSecondary,
    secondaryContainer = ObrisSecondaryContainer,
    onSecondaryContainer = ObrisOnSecondaryContainer,
    tertiary = ObrisTertiary,
    onTertiary = ObrisOnTertiary,
    tertiaryContainer = ObrisTertiaryContainer,
    onTertiaryContainer = ObrisOnTertiaryContainer,
    error = ObrisError,
    onError = ObrisOnError,
    errorContainer = Color(0xFF400010),
    onErrorContainer = Color(0xFFFFDAD6),
    background = ObrisBackground,
    onBackground = ObrisOnBackground,
    surface = ObrisSurface,
    onSurface = ObrisOnSurface,
    surfaceVariant = ObrisSurfaceVariant,
    onSurfaceVariant = ObrisOnSurfaceVariant,
    outline = ObrisOutline,
    outlineVariant = ObrisOutlineVariant,
    inverseSurface = ObrisInverseSurface,
    inverseOnSurface = ObrisInverseOnSurface,
    inversePrimary = ObrisInversePrimary,
    surfaceDim = Color(0xFF060406),
    surfaceBright = ObrisSurfaceVariant,
    surfaceContainerLowest = Color(0xFF060406),
    surfaceContainerLow = Color(0xFF0E0A0C),
    surfaceContainer = ObrisSurfaceContainer,
    surfaceContainerHigh = ObrisSurfaceContainerHigh,
    surfaceContainerHighest = ObrisSurfaceContainerHighest,
)

/** Theme mode enum */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Obris theme — dark only, full screen gaming */
@Composable
fun ObrisTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ObrisDarkScheme,
        typography = ObrisTypography,
        content = content,
    )
}
