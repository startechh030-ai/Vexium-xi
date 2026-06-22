package lux.vexium.app.feature.auth.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import lux.vexium.app.R

enum class PinMode {
    CREATE,     // First time — enter new PIN
    CONFIRM,    // Re-enter to confirm
    VERIFY,     // Returning user — enter existing PIN
}

@Composable
fun PinScreen(
    mode: PinMode,
    onPinConfirmed: (String) -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()

    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var currentMode by remember { mutableStateOf(mode) }
    var error by remember { mutableStateOf<String?>(null) }
    var shake by remember { mutableStateOf(false) }

    val title = when (currentMode) {
        PinMode.CREATE -> "Create your PIN"
        PinMode.CONFIRM -> "Confirm your PIN"
        PinMode.VERIFY -> "Enter your PIN"
    }

    val subtitle = when (currentMode) {
        PinMode.CREATE -> "Choose a 6-digit security code"
        PinMode.CONFIRM -> "Re-enter your code to confirm"
        PinMode.VERIFY -> "Enter your 6-digit code to continue"
    }

    // Handle PIN completion
    LaunchedEffect(pin) {
        if (pin.length == 6) {
            delay(200) // Brief pause for visual feedback

            when (currentMode) {
                PinMode.CREATE -> {
                    firstPin = pin
                    pin = ""
                    currentMode = PinMode.CONFIRM
                    error = null
                }
                PinMode.CONFIRM -> {
                    if (pin == firstPin) {
                        onPinConfirmed(pin)
                    } else {
                        error = "PINs don't match. Try again."
                        shake = true
                        pin = ""
                        currentMode = PinMode.CREATE
                        firstPin = ""
                        delay(500)
                        shake = false
                    }
                }
                PinMode.VERIFY -> {
                    onPinConfirmed(pin)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        colors = listOf(Color.Black, Color(0xFF0A0A0A), Color(0xFF080808)),
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF2F6FA), Color(0xFFE8F0F6), Color(0xFFF2F6FA)),
                    )
                },
            ),
    ) {
        // Back button
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier
                .padding(start = 16.dp, top = 48.dp)
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onBackClick)
                .padding(2.dp),
            tint = if (isDark) Color(0xFF888888) else Color(0xFF666666),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.25f))

            // Lock icon
            Icon(
                painter = painterResource(id = R.drawable.ic_fingerprint),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111111),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color(0xFF666666) else Color(0xFF8A9BB0),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(6) { index ->
                    PinDot(
                        filled = index < pin.length,
                        isDark = isDark,
                        hasError = error != null,
                    )
                }
            }

            // Error message
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF4444),
                textAlign = TextAlign.Center,
                minLines = 1,
            )

            Spacer(modifier = Modifier.weight(0.3f))

            // Custom number keyboard
            NumberKeyboard(
                onNumberClick = { num ->
                    if (pin.length < 6) {
                        pin += num
                        error = null
                    }
                },
                onDeleteClick = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                },
                onBiometricClick = if (currentMode == PinMode.VERIFY) onBiometricClick else null,
                isDark = isDark,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PinDot(filled: Boolean, isDark: Boolean, hasError: Boolean) {
    val color by animateColorAsState(
        targetValue = when {
            hasError -> Color(0xFFFF4444)
            filled && isDark -> Color(0xFF5EB0EF)
            filled -> Color(0xFF2A6FAC)
            isDark -> Color(0xFF222222)
            else -> Color(0xFFDDE2E8)
        },
        animationSpec = tween(150),
        label = "dot_color",
    )

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (!filled) {
                    Modifier.border(
                        1.dp,
                        if (isDark) Color(0xFF333333) else Color(0xFFCCD0D6),
                        CircleShape,
                    )
                } else Modifier,
            ),
    )
}

@Composable
private fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    isDark: Boolean,
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del"),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    when (key) {
                        "del" -> {
                            KeyButton(
                                isDark = isDark,
                                onClick = onDeleteClick,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isDark) Color(0xFF888888) else Color(0xFF666666),
                                )
                            }
                        }
                        "bio" -> {
                            if (onBiometricClick != null) {
                                KeyButton(
                                    isDark = isDark,
                                    onClick = onBiometricClick,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_fingerprint),
                                        contentDescription = "Biometric",
                                        modifier = Modifier.size(26.dp),
                                        tint = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC),
                                    )
                                }
                            } else {
                                // Empty space
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        else -> {
                            KeyButton(
                                isDark = isDark,
                                onClick = { onNumberClick(key) },
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White else Color(0xFF111111),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    isDark: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0xFF0F0F0F) else Color.White.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 36.dp),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
