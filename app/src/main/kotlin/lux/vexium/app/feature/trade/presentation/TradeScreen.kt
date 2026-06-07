package lux.vexium.app.feature.trade.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lux.vexium.app.core.theme.DarkCard
import lux.vexium.app.core.theme.DarkNavy
import lux.vexium.app.core.theme.NeonCyan
import lux.vexium.app.core.theme.NeonGreen
import lux.vexium.app.core.theme.NeonRed
import lux.vexium.app.core.theme.TextSecondary

data class TokenData(
    val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val isPositive: Boolean,
)

private val sampleTokens = listOf(
    TokenData("VXM", "Vexium Token", "$0.42", "+12.5%", true),
    TokenData("BTC", "Bitcoin", "$68,420", "+2.3%", true),
    TokenData("ETH", "Ethereum", "$3,850", "-1.2%", false),
    TokenData("SOL", "Solana", "$178", "+5.7%", true),
    TokenData("MATIC", "Polygon", "$1.24", "-0.8%", false),
)

@Composable
fun TradeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "📊 Trade",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Buy, sell, and swap tokens",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Token List ──
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sampleTokens.size) { index ->
                TokenCard(token = sampleTokens[index])
            }
        }
    }
}

@Composable
private fun TokenCard(token: TokenData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Token icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = token.symbol.take(2),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = token.price,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = token.change,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (token.isPositive) NeonGreen else NeonRed,
                )
            }
        }
    }
}
