package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.model.SpeechState
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraIndigoDeep
import com.example.ui.theme.AuraViolet
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    speechState: SpeechState,
    rmsAmplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 16
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val waveStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveStep"
    )

    Row(
        modifier = modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isActive = speechState == SpeechState.LISTENING || speechState == SpeechState.SPEAKING

        for (i in 0 until barCount) {
            val factor = sin(waveStep + (i * 0.4f)).toFloat()
            val heightFraction = if (isActive) {
                val ampFactor = if (speechState == SpeechState.LISTENING) (0.25f + rmsAmplitude * 0.75f) else 0.6f
                (0.2f + 0.8f * (factor + 1f) / 2f * ampFactor).coerceIn(0.15f, 1f)
            } else {
                0.12f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((36 * heightFraction).dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isActive) listOf(AuraCyan, AuraIndigo, AuraIndigoDeep)
                            else listOf(AuraIndigoDeep.copy(alpha = 0.3f), AuraCyan.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
