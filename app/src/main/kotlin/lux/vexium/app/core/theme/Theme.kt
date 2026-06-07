package lux.vexium.app.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Vexium is dark-theme only (gaming aesthetic)
private val VexiumColorScheme = darkColorScheme(
    // Primary
    primary = NeonCyan,
    onPrimary = DarkNavy,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = NeonCyanLight,

    // Secondary
    secondary = NeonPurple,
    onSecondary = DarkNavy,
    secondaryContainer = NeonPurpleDark,
    onSecondaryContainer = NeonPurpleLight,

    // Tertiary
    tertiary = NeonGreen,
    onTertiary = DarkNavy,
    tertiaryContainer = NeonGreenDark,
    onTertiaryContainer = NeonGreen,

    // Error
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Background
    background = DarkNavy,
    onBackground = TextPrimary,

    // Surface
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceLight,
    onSurfaceVariant = TextSecondary,

    // Outline
    outline = TextTertiary,
    outlineVariant = DarkCard,

    // Inverse
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkNavy,
    inversePrimary = NeonCyanDark,

    // Surface containers
    surfaceDim = DarkNavy,
    surfaceBright = DarkSurfaceLight,
    surfaceContainerLowest = Color(0xFF060914),
    surfaceContainerLow = DarkNavyLight,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceLight,
    surfaceContainerHighest = DarkCard,
)

@Composable
fun VexiumTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = VexiumColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VexiumTypography,
        content = content,
    )
}
