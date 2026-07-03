package lux.obris.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lux.obris.app.R

@Composable
fun BiometricSetupScreen(
    onEnable: () -> Unit,
    onSkip: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) {
                    Brush.verticalGradient(listOf(Color.Black, Color(0xFF0A0A0A)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFF2F6FA), Color(0xFFE8F0F6)))
                },
            )
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(0.4f))

        Icon(
            painter = painterResource(id = R.drawable.ic_fingerprint),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Enable Biometrics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF111111),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Use your fingerprint or face to unlock\nObris quickly next time",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDark) Color(0xFF666666) else Color(0xFF8A9BB0),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(0.5f))

        Button(
            onClick = onEnable,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF1A3A5C),
                contentColor = Color.White,
            ),
        ) {
            Text("Enable Biometrics", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8EAEC),
                contentColor = if (isDark) Color(0xFF888888) else Color(0xFF666666),
            ),
        ) {
            Text("Maybe Later", fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
