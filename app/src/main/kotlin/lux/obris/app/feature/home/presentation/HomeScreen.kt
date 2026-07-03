package lux.obris.app.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Home screen — landscape layout for Obris.
 * Shows featured games and quick actions.
 */
@Composable
fun HomeScreen(
    onNavigateToGames: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp),
    ) {
        // ── Left side: Welcome + Logout ──
        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome to Obris 🎮",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Play • Compete • Win",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorScheme.error.copy(alpha = 0.1f)),
            ) {
                Icon(Icons.Default.Logout, "Logout", tint = colorScheme.error)
            }
        }

        // ── Right side: Game cards ──
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            val gameNames = listOf("Speed Rush", "Memory Match", "Trivia Battle")
            val gameColors = listOf(colorScheme.primary, colorScheme.secondary, colorScheme.tertiary)

            gameNames.forEachIndexed { index, name ->
                Card(
                    onClick = onNavigateToGames,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(gameColors[index].copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.SportsEsports, null, tint = gameColors[index])
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Skill-based • Competitive", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        }
                        Text("Play →", style = MaterialTheme.typography.labelLarge, color = colorScheme.primary)
                    }
                }
            }
        }
    }
}
