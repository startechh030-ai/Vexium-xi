package lux.vexium.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.vexium.app.R

@Composable
fun TelegramCodeScreen(
    onSubmitCode: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val accentColor = Color(0xFF5EB0EF)
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick)
                    .padding(2.dp),
                tint = Color(0xFF666666),
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Telegram icon
        Icon(
            painter = painterResource(id = R.drawable.ic_telegram),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter Telegram Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Paste the code you received from\n@vexcchain_bot on Telegram",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("XXXXXXXX", color = Color(0xFF333333), letterSpacing = 4.sp)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFF222222),
                focusedContainerColor = Color(0xFF0A0A0A),
                unfocusedContainerColor = Color(0xFF0A0A0A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { if (code.length == 8) onSubmitCode(code) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(26.dp),
            enabled = code.length == 8,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.Black,
                disabledContainerColor = accentColor.copy(alpha = 0.2f),
                disabledContentColor = Color.White.copy(alpha = 0.3f),
            ),
        ) {
            Text("Verify Code", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}
