package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.SpeechState
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanBright
import com.example.ui.theme.AuraCyanGlow
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraIndigoDeep
import com.example.ui.theme.AuraIndigoGlow
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraRed
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.AuraVioletGlow
import kotlin.math.sin

@Composable
fun AuraVoiceOrb(
    speechState: SpeechState,
    rmsAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraOrbAnim")

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbWave"
    )

    val baseScale = when (speechState) {
        SpeechState.LISTENING -> 1.15f + (rmsAmplitude * 0.45f)
        SpeechState.SPEAKING -> 1.08f + (pulseAnim * 0.08f)
        SpeechState.PROCESSING -> 1.05f
        SpeechState.ERROR -> 0.98f
        SpeechState.IDLE -> pulseAnim
    }

    val primaryOrbColor = when (speechState) {
        SpeechState.LISTENING -> AuraCyan
        SpeechState.SPEAKING -> AuraIndigo
        SpeechState.PROCESSING -> AuraViolet
        SpeechState.ERROR -> AuraRed
        SpeechState.IDLE -> AuraCyanBright
    }

    val secondaryOrbColor = when (speechState) {
        SpeechState.LISTENING -> AuraIndigoDeep
        SpeechState.SPEAKING -> AuraCyan
        SpeechState.PROCESSING -> AuraIndigo
        SpeechState.ERROR -> Color(0xFFB91C1C)
        SpeechState.IDLE -> AuraIndigoDeep
    }

    Box(
        modifier = modifier
            .size(190.dp)
            .testTag("aura_voice_orb_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = false, radius = 95.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2.7f) * baseScale

            // Outer Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = 0.35f),
                        secondaryOrbColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.55f
                ),
                radius = radius * 1.55f,
                center = center
            )

            // Expanding ripple wave when listening or speaking
            if (speechState == SpeechState.LISTENING || speechState == SpeechState.SPEAKING) {
                val rippleRadius = radius + (waveAnim * 35.dp.toPx())
                val rippleAlpha = (1f - waveAnim).coerceIn(0f, 0.7f)
                drawCircle(
                    color = primaryOrbColor.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Orbital Ring 1
            rotate(rotationAnim, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryOrbColor.copy(alpha = 0.8f),
                            secondaryOrbColor.copy(alpha = 0.2f),
                            primaryOrbColor.copy(alpha = 0.9f)
                        ),
                        center = center
                    ),
                    radius = radius * 1.12f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Orbital Ring 2 (counter-rotating)
            rotate(-rotationAnim * 1.3f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            secondaryOrbColor.copy(alpha = 0.7f),
                            AuraCyanGlow,
                            secondaryOrbColor.copy(alpha = 0.8f)
                        ),
                        center = center
                    ),
                    radius = radius * 1.25f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Core Solid Radiant Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        primaryOrbColor,
                        secondaryOrbColor,
                        Color(0xFF0F172A)
                    ),
                    center = center.copy(y = center.y - (radius * 0.2f)),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Dynamic Plasma Energy Nodes
            for (i in 0 until 6) {
                val angle = (i * 60f + rotationAnim * 2f) * (Math.PI / 180f)
                val nodeDistance = radius * 0.7f * (0.85f + 0.15f * sin(angle * 3 + waveAnim * 6).toFloat())
                val nodeOffset = Offset(
                    x = center.x + (nodeDistance * kotlin.math.cos(angle)).toFloat(),
                    y = center.y + (nodeDistance * kotlin.math.sin(angle)).toFloat()
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = nodeOffset
                )
            }
        }

        // Center Icon
        val centerIcon = when (speechState) {
            SpeechState.LISTENING -> Icons.Default.GraphicEq
            SpeechState.SPEAKING -> Icons.Default.VolumeUp
            SpeechState.PROCESSING -> Icons.Default.HourglassTop
            SpeechState.ERROR -> Icons.Default.Warning
            SpeechState.IDLE -> Icons.Default.Mic
        }

        Icon(
            imageVector = centerIcon,
            contentDescription = "MAX Voice Trigger",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}
