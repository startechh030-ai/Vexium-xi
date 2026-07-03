package lux.obris.app.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Cyberpunk Space Dark Scheme ──
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
    surfaceDim = Color(0xFF000000),
    surfaceBright = ObrisSurfaceVariant,
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF060610),
    surfaceContainer = ObrisSurfaceContainer,
    surfaceContainerHigh = ObrisSurfaceContainerHigh,
    surfaceContainerHighest = ObrisSurfaceContainerHighest,
)

/** Theme mode — dark only for now */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Obris app theme — cyberpunk space, dark only.
 */
@Composable
fun ObrisTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val colorScheme = ObrisDarkScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ObrisTypography,
        content = content,
    )
}
