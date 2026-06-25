package lux.vexium.app.feature.auth.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import lux.vexium.app.R

enum class PinMode { CREATE, CONFIRM, VERIFY }

@Composable
fun PinScreen(
    mode: PinMode,
    userName: String? = null,
    userInitials: String? = null,
    onPinConfirmed: (String) -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    onLogout: (() -> Unit)? = null,
) {
    val isDark = isSystemInDarkTheme()
    val accentColor = if (isDark) Color(0xFF5EB0EF) else Color(0xFF2A6FAC)

    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var currentMode by remember { mutableStateOf(mode) }
    var error by remember { mutableStateOf<String?>(null) }

    val title = when (currentMode) {
        PinMode.CREATE -> "Create your PIN"
        PinMode.CONFIRM -> "Confirm your PIN"
        PinMode.VERIFY -> if (userName != null) "Welcome Back" else "Enter your PIN"
    }

    val subtitle = when (currentMode) {
        PinMode.CREATE -> "Choose a 6-digit security code"
        PinMode.CONFIRM -> "Re-enter to confirm"
        PinMode.VERIFY -> "Enter your 6-digit PIN"
    }

    LaunchedEffect(pin) {
        if (pin.length == 6) {
            delay(200)
            when (currentMode) {
                PinMode.CREATE -> {
                    firstPin = pin; pin = ""; currentMode = PinMode.CONFIRM; error = null
                }
                PinMode.CONFIRM -> {
                    if (pin == firstPin) {
                        onPinConfirmed(pin)
                    } else {
                        error = "PINs don't match"; pin = ""; currentMode = PinMode.CREATE; firstPin = ""
                        delay(500)
                    }
                }
                PinMode.VERIFY -> onPinConfirmed(pin)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Ambient background ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            if (isDark) {
                // Subtle blue ambient top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1A3050).copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.15f), radius = w * 0.5f,
                    ),
                    radius = w * 0.5f, center = Offset(w * 0.8f, h * 0.15f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0D2040).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.85f), radius = w * 0.4f,
                    ),
                    radius = w * 0.4f, center = Offset(w * 0.15f, h * 0.85f),
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD0E8F8).copy(alpha = 0.40f), Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.12f), radius = w * 0.5f,
                    ),
                    radius = w * 0.5f, center = Offset(w * 0.8f, h * 0.12f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD8ECF8).copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(w * 0.2f, h * 0.8f), radius = w * 0.35f,
                    ),
                    radius = w * 0.35f, center = Offset(w * 0.2f, h * 0.8f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black.copy(alpha = 0.85f) else Color(0xFFF2F6FA).copy(alpha = 0.90f))
                .padding(horizontal = 24.dp),
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
                    tint = if (isDark) Color(0xFF666666) else Color(0xFF999999),
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Avatar (verify mode only)
            if (currentMode == PinMode.VERIFY && userInitials != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFE4E8EC))
                        .border(2.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = userInitials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Lock icon for create mode
                Icon(
                    painter = painterResource(id = R.drawable.ic_fingerprint),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = accentColor,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            if (currentMode == PinMode.VERIFY && userName != null) {
                Text(
                    text = "Welcome Back, ${userName.split(" ").first()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111111),
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111111),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN circles (outlined style like reference)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(6) { index ->
                    PinCircle(filled = index < pin.length, isDark = isDark, hasError = error != null, accentColor = accentColor)
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF5555))
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Number keyboard
            NumberKeyboard(
                onNumberClick = { if (pin.length < 6) { pin += it; error = null } },
                onDeleteClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                onBiometricClick = if (currentMode == PinMode.VERIFY) onBiometricClick else null,
                isDark = isDark,
                accentColor = accentColor,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom actions (verify mode)
            if (currentMode == PinMode.VERIFY && onLogout != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onLogout)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                    Text(
                        text = "  |  ",
                        color = if (isDark) Color(0xFF333333) else Color(0xFFCCCCCC),
                    )
                    Text(
                        text = "Forgot PIN? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color(0xFF555555) else Color(0xFF999999),
                    )
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { /* TODO: Reset PIN flow */ }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PinCircle(filled: Boolean, isDark: Boolean, hasError: Boolean, accentColor: Color) {
    val fillColor by animateColorAsState(
        targetValue = when {
            hasError -> Color(0xFFFF5555)
            filled -> accentColor
            else -> Color.Transparent
        },
        animationSpec = tween(150), label = "fill",
    )
    val borderColor = when {
        hasError -> Color(0xFFFF5555)
        filled -> accentColor
        isDark -> Color(0xFF333333)
        else -> Color(0xFFCCD0D6)
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(fillColor)
            .border(1.5.dp, borderColor, CircleShape),
    )
}

@Composable
private fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    isDark: Boolean,
    accentColor: Color,
) {
    val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("bio","0","del"))

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { key ->
                    when (key) {
                        "del" -> KeyBtn(isDark, onClick = onDeleteClick) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, null, Modifier.size(22.dp), tint = if (isDark) Color(0xFF666666) else Color(0xFF999999))
                        }
                        "bio" -> if (onBiometricClick != null) {
                            KeyBtn(isDark, onClick = onBiometricClick) {
                                Icon(painterResource(R.drawable.ic_fingerprint), null, Modifier.size(24.dp), tint = accentColor)
                            }
                        } else {
                            Spacer(modifier = Modifier.size(68.dp))
                        }
                        else -> KeyBtn(isDark, onClick = { onNumberClick(key) }) {
                            Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF111111))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyBtn(isDark: Boolean, onClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0xFF0D0D0D) else Color.White.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 34.dp),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) { content() }
}
