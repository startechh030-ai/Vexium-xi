package lux.obris.app.feature.auth.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lux.obris.app.R

// ── Auth colors — ice blue, white, cool grey ──
private val IceBlue = Color(0xFF7DD3FC)
private val IceBlueDim = Color(0xFF38BDF8)
private val CoolWhite = Color(0xFFF0F4F8)
private val NeonGrid = Color(0xFF38BDF8)
private val BtnBg = Color(0xFF0C1420)
private val BtnBorder = Color(0xFF1E3A5F)

/**
 * Auth layout — transparent overlay, takes bottom 40% of screen.
 * Circuit grid lines animate along edges.
 * Google (wide), Email | Lux | More (row below).
 *
 * @param onGoogleClick Google sign-in callback
 * @param onEmailClick Email auth callback
 * @param onGuestClick Guest/Lux callback
 * @param onMoreClick More options callback
 */
@Composable
fun AuthLayout(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    // Circuit line animation
    val inf = rememberInfiniteTransition(label = "circuit")
    val circuitProgress by inf.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "circuit",
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // ── Circuit grid lines — subtle neon along edges ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val lineAlpha = 0.06f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), phase = circuitProgress * 200f)

            // Bottom horizontal
            drawLine(NeonGrid.copy(alpha = lineAlpha), Offset(0f, h * 0.6f), Offset(w, h * 0.6f), 0.8f, pathEffect = dashEffect)
            // Left vertical
            drawLine(NeonGrid.copy(alpha = lineAlpha * 0.7f), Offset(w * 0.04f, h * 0.6f), Offset(w * 0.04f, h), 0.8f, pathEffect = dashEffect)
            // Right vertical
            drawLine(NeonGrid.copy(alpha = lineAlpha * 0.7f), Offset(w * 0.96f, h * 0.6f), Offset(w * 0.96f, h), 0.8f, pathEffect = dashEffect)
            // Bottom edge
            drawLine(NeonGrid.copy(alpha = lineAlpha), Offset(0f, h * 0.98f), Offset(w, h * 0.98f), 0.8f, pathEffect = dashEffect)

            // Moving dot along the circuit
            val dotX = w * circuitProgress
            drawCircle(NeonGrid.copy(alpha = 0.15f), 2.5f, Offset(dotX, h * 0.6f))
        }

        // ── Auth buttons — bottom 40% ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            // ── Google — wide button with sharp cut corners ──
            val googleShape = CutCornerShape(topStart = 2.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 2.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(44.dp)
                    .clip(googleShape)
                    .background(CoolWhite)
                    .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGoogleClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(painterResource(R.drawable.ic_google), null, Modifier.size(16.dp), tint = Color.Unspecified)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Sign in with Google",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E)),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Email | Lux | More — three buttons in a row ──
            Row(
                modifier = Modifier.fillMaxWidth(0.48f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Email
                val smallShape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(smallShape)
                        .background(BtnBg.copy(alpha = 0.85f))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onEmailClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Email", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = IceBlue))
                }

                // Lux (Guest)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(smallShape)
                        .background(IceBlueDim.copy(alpha = 0.12f))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGuestClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Guest", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoolWhite))
                }

                // More
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(smallShape)
                        .background(BtnBg.copy(alpha = 0.85f))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onMoreClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("More", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = IceBlue))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legal
            Text(
                "By continuing, you agree to the Terms of Service and Privacy Policy.",
                style = TextStyle(fontSize = 8.sp, color = Color.White.copy(alpha = 0.20f)),
            )
        }
    }
}
