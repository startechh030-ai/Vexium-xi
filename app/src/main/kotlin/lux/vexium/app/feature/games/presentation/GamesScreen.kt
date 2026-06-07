package lux.vexium.app.feature.games.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lux.vexium.app.core.theme.DarkCard
import lux.vexium.app.core.theme.DarkNavy
import lux.vexium.app.core.theme.NeonCyan
import lux.vexium.app.core.theme.NeonGreen
import lux.vexium.app.core.theme.NeonOrange
import lux.vexium.app.core.theme.NeonPurple
import lux.vexium.app.core.theme.TextSecondary

data class GameItem(
    val id: String,
    val name: String,
    val category: String,
    val reward: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
)

private val sampleGames = listOf(
    GameItem("1", "Crypto Trivia", "Trivia", "50 VXM", Icons.Default.QuestionMark, NeonCyan),
    GameItem("2", "Memory Match", "Puzzle", "30 VXM", Icons.Default.Extension, NeonPurple),
    GameItem("3", "Tap Frenzy", "Speed", "25 VXM", Icons.Default.Speed, NeonGreen),
    GameItem("4", "Number Rush", "Math", "40 VXM", Icons.Default.EmojiEvents, NeonOrange),
)

private val categories = listOf("All", "Trivia", "Puzzle", "Speed", "Math")

@Composable
fun GamesScreen(
    onNavigateToGameDetail: (String) -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🎮 Game Hub",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Search Bar ──
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search games...", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkCard,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard,
            ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Category Filters ──
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    label = { Text(categories[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Games List ──
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val filteredGames = if (selectedCategory == 0) {
                sampleGames
            } else {
                sampleGames.filter { it.category == categories[selectedCategory] }
            }

            items(filteredGames) { game ->
                GameCard(game = game, onClick = { onNavigateToGameDetail(game.id) })
            }
        }
    }
}

@Composable
private fun GameCard(
    game: GameItem,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(game.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = game.icon,
                    contentDescription = null,
                    tint = game.color,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = game.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = game.reward,
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Reward",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }
    }
}
