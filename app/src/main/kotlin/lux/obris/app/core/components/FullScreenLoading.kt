package lux.obris.app.core.components

import androidx.compose.runtime.Composable

/**
 * Full-screen loading — uses the gamified hexagonal spinner.
 * Used during auth transitions.
 */
@Composable
fun FullScreenLoading(
    message: String = "Loading...",
) {
    GamefyLoading(message = message)
}
