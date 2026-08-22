package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeechState
import com.example.ui.components.AdiAvatarView
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AuraBackgroundDark
import com.example.ui.theme.AuraCardBorder
import com.example.ui.theme.AuraCardGlassHigh
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanBright
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraNeonGreen
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel
import com.example.viewmodel.BackgroundServiceStatus

@Composable
fun VoiceHubScreen(
    viewModel: AuraViewModel,
    uiState: AuraUiState,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCommands: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val quickSuggestions = listOf(
        "MAX, call receive karo",
        "MAX, call reject karo",
        "Speaker on karo",
        "Speaker off karo",
        "Call utha lo",
        "Call kaat do",
        "WhatsApp kholo",
        "WiFi on kar",
        "Papa ko call laga do",
        "Volume 50 percent karo",
        "Alarm kal subah 7 baje ka laga do",
        "Chrome me YouTube kholo"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackgroundDark)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .widthIn(max = 600.dp)
        ) {
            // Top Status Bar: Background Service Status & Controls (Active / Permission Required / Stopped)
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (uiState.backgroundServiceStatus) {
                            BackgroundServiceStatus.ACTIVE -> AuraNeonGreen.copy(alpha = 0.5f)
                            BackgroundServiceStatus.PERMISSION_REQUIRED -> Color(0xFFF59E0B).copy(alpha = 0.5f)
                            BackgroundServiceStatus.STOPPED -> Color(0xFFEF4444).copy(alpha = 0.5f)
                        },
                        RoundedCornerShape(18.dp)
                    )
                    .testTag("card_background_service_status")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Status Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState.backgroundServiceStatus) {
                                            BackgroundServiceStatus.ACTIVE -> AuraNeonGreen
                                            BackgroundServiceStatus.PERMISSION_REQUIRED -> Color(0xFFF59E0B)
                                            BackgroundServiceStatus.STOPPED -> Color(0xFFEF4444)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (uiState.backgroundServiceStatus) {
                                        BackgroundServiceStatus.ACTIVE -> "🟢 Background Service: Active"
                                        BackgroundServiceStatus.PERMISSION_REQUIRED -> "🟡 Background: Permission Required"
                                        BackgroundServiceStatus.STOPPED -> "🔴 Background Service: Stopped"
                                    },
                                    color = when (uiState.backgroundServiceStatus) {
                                        BackgroundServiceStatus.ACTIVE -> AuraNeonGreen
                                        BackgroundServiceStatus.PERMISSION_REQUIRED -> Color(0xFFF59E0B)
                                        BackgroundServiceStatus.STOPPED -> Color(0xFFEF4444)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Wake word: “MAX” / “Hey MAX” • Continuous bg standby",
                                    color = AuraTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Wake Word Active Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AuraCyan.copy(alpha = 0.12f))
                                .border(1.dp, AuraCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Hey MAX",
                                color = AuraCyanBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Action Controls: Start / Stop / Restart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start Button
                        Button(
                            onClick = { viewModel.startBackgroundService() },
                            enabled = uiState.backgroundServiceStatus != BackgroundServiceStatus.ACTIVE,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraNeonGreen,
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0xFF1E293B),
                                disabledContentColor = AuraTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_start_service")
                        ) {
                            Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Stop Button
                        Button(
                            onClick = { viewModel.stopBackgroundService() },
                            enabled = uiState.backgroundServiceStatus == BackgroundServiceStatus.ACTIVE,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E293B),
                                disabledContentColor = AuraTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_stop_service")
                        ) {
                            Text("Stop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Restart Button
                        Button(
                            onClick = { viewModel.restartBackgroundService() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraCyan.copy(alpha = 0.25f),
                                contentColor = AuraCyanBright
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_restart_service")
                        ) {
                            Text("Restart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ADI AI Avatar with dynamic aura
            AdiAvatarView(
                speechState = uiState.speechState,
                rmsAmplitude = uiState.rmsAmplitude,
                avatarType = uiState.avatarType,
                onClick = { viewModel.toggleListening() },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("adi_avatar_view")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Waveform visualizer
            WaveformVisualizer(
                speechState = uiState.speechState,
                rmsAmplitude = uiState.rmsAmplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live Transcript & Assistant Response Box
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Transcript
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Transcript",
                            tint = AuraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOU SAID",
                            color = AuraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.liveTranscript.isBlank()) "Tap mic or say 'MAX'..." else "\"${uiState.liveTranscript}\"",
                        color = if (uiState.liveTranscript.isBlank()) AuraTextMuted else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Response
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "MAX Response",
                                tint = AuraNeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MAX RESPONSE",
                                color = AuraNeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.replayAssistantVoice() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay voice",
                                tint = AuraCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.assistantResponse,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    )
                }
            }

            // Pending Confirmation Alert (Level 2/3 Safety)
            AnimatedVisibility(
                visible = uiState.pendingConfirmation != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.pendingConfirmation?.let { pending ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Confirmation",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Permission Confirmation Required",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pending.confirmationPrompt,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.confirmPendingAction(false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Nahi / Cancel")
                                }
                                Button(
                                    onClick = { viewModel.confirmPendingAction(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright)
                                ) {
                                    Text("Haan / Confirm", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main "Talk to ADI" Glowing Button
            Button(
                onClick = { viewModel.toggleListening() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.speechState == SpeechState.LISTENING) Color(0xFFEF4444) else AuraCyanBright
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        16.dp,
                        RoundedCornerShape(24.dp),
                        spotColor = if (uiState.speechState == SpeechState.LISTENING) Color(0xFFEF4444) else AuraCyanBright
                    )
                    .testTag("talk_to_max_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.speechState == SpeechState.LISTENING) Icons.Default.Close else Icons.Default.Mic,
                        contentDescription = "Talk to MAX",
                        tint = if (uiState.speechState == SpeechState.LISTENING) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.speechState == SpeechState.LISTENING) "Listening... Tap to Stop" else "Talk to MAX",
                        color = if (uiState.speechState == SpeechState.LISTENING) Color.White else Color(0xFF0F172A),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6 Primary Quick Action Buttons
            Text(
                text = "QUICK CONTROL TILES",
                color = AuraTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionTile(
                    title = "Call",
                    subtitle = "Papa / Contact",
                    icon = Icons.Default.Call,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Papa ko call laga do") }
                )
                QuickActionTile(
                    title = "Message",
                    subtitle = "WhatsApp / SMS",
                    icon = Icons.Default.Chat,
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("WhatsApp par Rahul ko Hello message bhejo") }
                )
                QuickActionTile(
                    title = "Apps",
                    subtitle = "YouTube / Chrome",
                    icon = Icons.Default.Apps,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("YouTube kholo") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionTile(
                    title = "Search",
                    subtitle = "Google & Maps",
                    icon = Icons.Default.Search,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Google par latest tech news search karo") }
                )
                QuickActionTile(
                    title = "Alarm",
                    subtitle = "Set 7:00 AM",
                    icon = Icons.Default.Alarm,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Alarm subah 7 baje ka laga do") }
                )
                QuickActionTile(
                    title = "Camera",
                    subtitle = "Photo / Video",
                    icon = Icons.Default.CameraAlt,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.processCommand("Camera open karo") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📞 Call Control & Hands-Free Deck
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("card_call_control_deck")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AuraNeonGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Control",
                                    tint = AuraNeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hands-Free Call Control",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Receive, Reject, End & Speaker via Voice",
                                    color = AuraTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.simulateIncomingCall("Papa", "+91 98765 43210") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuraNeonGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_test_incoming_call")
                        ) {
                            Text("Test Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Voice Commands:",
                        color = AuraCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        listOf(
                            "“MAX, call receive karo”" to "MAX, call receive karo",
                            "“Call utha lo”" to "Call utha lo",
                            "“MAX, call reject karo”" to "MAX, call reject karo",
                            "“Call kaat do”" to "Call kaat do",
                            "“Speaker on karo”" to "Speaker on karo",
                            "“Speaker off karo”" to "Speaker off karo"
                        ).forEach { (label, cmd) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF161F30))
                                    .border(1.dp, AuraCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.processCommand(cmd) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Suggestions Carousel
            Text(
                text = "TRY SAYING (HINDI & ENGLISH)",
                color = AuraTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (sug in quickSuggestions) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AuraCardGlassHigh)
                            .border(1.dp, AuraCardBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.processCommand(sug) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "\"$sug\"",
                            color = AuraTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // System Readiness Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
                    .clickable { onNavigateToSettings() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Assistant Readiness",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.grantedPermissionsCount}/${uiState.totalPermissionsCount} Permissions Enabled • Accessibility ${if (uiState.isAccessibilityEnabled) "Active" else "Off"}",
                            color = AuraTextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Manage",
                            color = AuraCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, AuraCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = AuraTextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
