package lux.obris.app.feature.welcome.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import lux.obris.app.R

/**
 * Welcome / Auth — Free Fire style.
 * Image fills every pixel. Auth at bottom center.
 */
@Composable
fun WelcomeScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
) {
    val bgPainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/screens/loading_bg_1.jpg")
            .build(),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Background — Crop fills every pixel ──
        Image(
            painter = bgPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── Bottom gradient ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                    startY = size.height * 0.4f,
                    endY = size.height,
                ),
                size = Size(size.width, size.height),
            )
        }

        // ── Version top-left ──
        Text(
            text = "v1.0.0",
            style = TextStyle(fontSize = 10.sp, color = Color.White.copy(alpha = 0.35f), letterSpacing = 1.sp),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        )

        // ── Auth buttons — bottom center ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Google — primary CTA
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGoogleClick)
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_google), null, Modifier.size(18.dp), tint = Color.Unspecified)
                Spacer(Modifier.width(10.dp))
                Text("Sign in with Google", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333)))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // or
            Row(Modifier.fillMaxWidth(0.3f), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
                Text("or", Modifier.padding(horizontal = 12.dp), style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f)))
                Box(Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guest + More
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onGuestClick)
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("👤", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Guest", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333)))
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(remember { MutableInteractionSource() }, ripple(bounded = true), onClick = onEmailClick)
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("••• More", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF333333)))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "I have read and agree to the Terms of Service and Privacy Policy.",
                style = TextStyle(fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f)),
            )
        }
    }
}
