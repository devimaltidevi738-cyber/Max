package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SpeechState
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanBright
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraNeonGreen

@Composable
fun AdiAvatarView(
    speechState: SpeechState,
    rmsAmplitude: Float,
    avatarType: String = "AI_AVATAR",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "adi_avatar_glow")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (speechState == SpeechState.LISTENING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (speechState == SpeechState.LISTENING) 0.85f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_glow_alpha"
    )

    val auraColor = when (speechState) {
        SpeechState.LISTENING -> AuraCyanBright
        SpeechState.PROCESSING -> AuraIndigo
        SpeechState.SPEAKING -> AuraNeonGreen
        SpeechState.ERROR -> Color(0xFFFF5252)
        SpeechState.IDLE -> AuraCyan
    }

    val stateBadgeText = when (speechState) {
        SpeechState.LISTENING -> "Listening..."
        SpeechState.PROCESSING -> "Thinking..."
        SpeechState.SPEAKING -> "Speaking..."
        SpeechState.ERROR -> "Need Attention"
        SpeechState.IDLE -> "🟢 Ready (MAX)"
    }

    val stateBadgeBg = when (speechState) {
        SpeechState.LISTENING -> Color(0xFF00E5FF).copy(alpha = 0.2f)
        SpeechState.PROCESSING -> Color(0xFF6366F1).copy(alpha = 0.25f)
        SpeechState.SPEAKING -> Color(0xFF00E676).copy(alpha = 0.2f)
        SpeechState.ERROR -> Color(0xFFFF5252).copy(alpha = 0.25f)
        SpeechState.IDLE -> Color(0xFF10B981).copy(alpha = 0.15f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Outer dynamic animated glow rings
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = glowAlpha),
                                auraColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner Ring Border
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (speechState == SpeechState.LISTENING) 3.dp else 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                AuraCyanBright,
                                AuraIndigo,
                                AuraCyan,
                                AuraCyanBright
                            )
                        ),
                        shape = CircleShape
                    )
                    .shadow(16.dp, CircleShape, spotColor = auraColor)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                // Avatar image
                Image(
                    painter = painterResource(id = R.drawable.ic_adi_avatar),
                    contentDescription = "MAX AI Persona Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                // Mic overlay indicator when listening
                if (speechState == SpeechState.LISTENING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF00E5FF).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // State indicator badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(stateBadgeBg)
                .border(1.dp, auraColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stateBadgeText,
                color = if (speechState == SpeechState.ERROR) Color(0xFFFF5252) else AuraCyanBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
