package lux.vexium.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameSheet(
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onContinue: (username: String, referralCode: String) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var username by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    val surfaceColor = if (isDark) Color(0xFF0C0C0C) else Color.White
    val fieldBg = if (isDark) Color(0xFF161616) else Color(0xFFF4F6F8)
    val fieldBorder = if (isDark) Color(0xFF2A2A2A) else Color(0xFFDDE2E8)
    val hintColor = if (isDark) Color(0xFF555555) else Color(0xFFAAB0B8)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(0.1f)
                        .background(
                            if (isDark) Color(0xFF333333) else Color(0xFFDDE2E8),
                            RoundedCornerShape(2.dp),
                        ),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "Set up your profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111111),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose a username to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = hintColor,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Username field
            Text(
                text = "Username",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("something_fantastic", color = hintColor, fontSize = 15.sp)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
                    unfocusedBorderColor = fieldBorder,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Referral field
            Text(
                text = "Referral Code",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = referralCode,
                onValueChange = { referralCode = it.uppercase().take(8) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("80% of users get rewards with a referral", color = hintColor, fontSize = 14.sp)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
                    unfocusedBorderColor = fieldBorder,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                ),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Skip
                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8EAEC),
                        contentColor = if (isDark) Color(0xFF888888) else Color(0xFF666666),
                    ),
                ) {
                    Text("Skip", fontWeight = FontWeight.Medium)
                }

                // Continue
                Button(
                    onClick = { onContinue(username, referralCode) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = username.length >= 3,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF1A3A5C),
                        contentColor = Color.White,
                        disabledContainerColor = if (isDark) Color(0xFF1A2A3A) else Color(0xFFB0C4D8),
                        disabledContentColor = if (isDark) Color(0xFF444444) else Color.White.copy(alpha = 0.6f),
                    ),
                ) {
                    Text("Continue", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
