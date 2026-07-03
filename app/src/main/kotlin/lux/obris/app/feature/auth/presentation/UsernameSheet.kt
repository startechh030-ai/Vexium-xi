package lux.obris.app.feature.auth.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

    val surfaceColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFBFC)
    val fieldBg = if (isDark) Color(0xFF141414) else Color(0xFFF0F3F6)
    val fieldBorder = if (isDark) Color(0xFF252525) else Color(0xFFDADEE4)
    val hintColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFFAAB0B8)
    val accentColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxHeight(0.85f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(0.08f)
                        .background(
                            if (isDark) Color(0xFF2A2A2A) else Color(0xFFD0D4DA),
                            RoundedCornerShape(2.dp),
                        ),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            // ── Illustration header with ambient glow ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Ambient glow orbs
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (isDark) 0.12f else 0.15f),
                                accentColor.copy(alpha = if (isDark) 0.04f else 0.05f),
                                Color.Transparent,
                            ),
                            center = Offset(w * 0.3f, h * 0.4f),
                            radius = w * 0.35f,
                        ),
                        radius = w * 0.35f,
                        center = Offset(w * 0.3f, h * 0.4f),
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = if (isDark) 0.08f else 0.10f),
                                Color(0xFF8B5CF6).copy(alpha = 0.02f),
                                Color.Transparent,
                            ),
                            center = Offset(w * 0.75f, h * 0.5f),
                            radius = w * 0.30f,
                        ),
                        radius = w * 0.30f,
                        center = Offset(w * 0.75f, h * 0.5f),
                    )

                    // Decorative floating particles
                    val particleColor = accentColor.copy(alpha = if (isDark) 0.15f else 0.20f)
                    drawCircle(color = particleColor, radius = 3f, center = Offset(w * 0.2f, h * 0.3f))
                    drawCircle(color = particleColor, radius = 2f, center = Offset(w * 0.6f, h * 0.2f))
                    drawCircle(color = particleColor, radius = 4f, center = Offset(w * 0.8f, h * 0.35f))
                    drawCircle(color = particleColor, radius = 2.5f, center = Offset(w * 0.4f, h * 0.15f))
                    drawCircle(color = particleColor, radius = 3f, center = Offset(w * 0.15f, h * 0.65f))
                    drawCircle(color = particleColor, radius = 2f, center = Offset(w * 0.9f, h * 0.6f))
                }

                // Center content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "👋",
                        fontSize = 48.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welcome to Obris",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF111111),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Let's set up your profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = hintColor,
                    )
                }
            }

            // ── Form fields ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Username
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }.take(20)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("something_fantastic", color = hintColor, fontSize = 15.sp)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (username.isNotEmpty()) accentColor else hintColor,
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = fieldBorder,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg,
                    ),
                    singleLine = true,
                )

                if (username.isNotEmpty() && username.length < 3) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "At least 3 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B6B),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Referral
                Text(
                    text = "Referral Code (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = referralCode,
                    onValueChange = { referralCode = it.uppercase().take(8) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("80% of users earn more with referrals", color = hintColor, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Redeem,
                            contentDescription = null,
                            tint = if (referralCode.isNotEmpty()) accentColor else hintColor,
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = fieldBorder,
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg,
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF181818) else Color(0xFFE8EAEC),
                            contentColor = if (isDark) Color(0xFF777777) else Color(0xFF666666),
                        ),
                    ) {
                        Text("Skip", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }

                    Button(
                        onClick = { onContinue(username, referralCode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = username.length >= 3,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White,
                            disabledContainerColor = accentColor.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.4f),
                        ),
                    ) {
                        Text("Continue", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
