package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telephony.ActiveCallData
import com.example.telephony.CallState
import com.example.ui.theme.AuraBackgroundDark
import com.example.ui.theme.AuraCardBorder
import com.example.ui.theme.AuraCyanGlow
import com.example.ui.theme.AuraNeonGreen
import com.example.ui.theme.AuraTextMuted

@Composable
fun IncomingCallHud(
    callState: ActiveCallData,
    onAnswerCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = callState.state == CallState.RINGING || callState.state == CallState.OFFHOOK,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        when (callState.state) {
            CallState.RINGING -> {
                IncomingRingingCard(
                    callerName = callState.callerName.ifBlank { "Incoming Call" },
                    callerNumber = callState.callerNumber.ifBlank { "Unknown Number" },
                    onAnswer = onAnswerCall,
                    onReject = onRejectCall
                )
            }
            CallState.OFFHOOK -> {
                ActiveCallCard(
                    callerName = callState.callerName.ifBlank { "Active Call" },
                    callerNumber = callState.callerNumber.ifBlank { "In Progress" },
                    durationSeconds = callState.callDurationSeconds,
                    isSpeakerOn = callState.isSpeakerOn,
                    onEndCall = onEndCall,
                    onToggleSpeaker = onToggleSpeaker
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun IncomingRingingCard(
    callerName: String,
    callerNumber: String,
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, AuraNeonGreen, RoundedCornerShape(24.dp))
            .testTag("incoming_call_card"),
        shape = RoundedCornerShape(24.dp),
        color = AuraBackgroundDark,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AuraNeonGreen.copy(alpha = 0.15f),
                            AuraBackgroundDark
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Incoming Call Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraNeonGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AuraNeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INCOMING CALL",
                    color = AuraNeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Pulsing Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(84.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AuraNeonGreen.copy(alpha = 0.25f))
                )
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraNeonGreen, AuraCyanGlow)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Caller Avatar",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caller Name & Number
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = callerNumber,
                color = AuraTextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Voice Command Prompts
            Surface(
                color = Color(0xFF141A29),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Guide",
                        tint = AuraCyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Say “MAX, call receive karo” or “MAX, call reject karo”",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject Button
                ElevatedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_reject_call")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Reject Call",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Answer / Receive Button
                ElevatedButton(
                    onClick = onAnswer,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = AuraNeonGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_receive_call")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Receive Call",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Receive", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ActiveCallCard(
    callerName: String,
    callerNumber: String,
    durationSeconds: Int,
    isSpeakerOn: Boolean,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, AuraCyanGlow, RoundedCornerShape(24.dp))
            .testTag("active_call_card"),
        shape = RoundedCornerShape(24.dp),
        color = AuraBackgroundDark,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AuraCyanGlow.copy(alpha = 0.12f),
                            AuraBackgroundDark
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Active Call Duration Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B5E20))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AuraNeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ON CALL  •  $formattedDuration",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = callerName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = callerNumber,
                color = AuraTextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Active call voice commands prompt
            Surface(
                color = Color(0xFF141A29),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Guide",
                        tint = AuraCyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Say “Speaker on karo” or “Call kaat do”",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker Button
                FilledTonalButton(
                    onClick = onToggleSpeaker,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSpeakerOn) AuraCyanGlow else Color(0xFF222938),
                        contentColor = if (isSpeakerOn) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .testTag("btn_speaker_toggle")
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeDown,
                        contentDescription = "Speaker Toggle",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpeakerOn) "Speaker ON" else "Speaker OFF",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // End Call Button
                ElevatedButton(
                    onClick = onEndCall,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .testTag("btn_end_call")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "End Call",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End Call", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
