package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.PermissionStatus

@Composable
fun SystemAccessScreen(
    viewModel: AuraViewModel,
    uiState: AuraUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Permissions") }
    val tabs = listOf("Permissions", "Voice & Wake", "Background & Battery", "Privacy & Security", "Help & About")

    var showClearDataDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackgroundDark)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 600.dp)
        ) {
            // Header
            Text(
                text = "Settings & Access",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Permissions, voice recognition, background engine & security",
                color = AuraTextMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (tab in tabs) {
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AuraCyanBright else AuraCardGlassHigh)
                            .border(1.dp, if (isSelected) AuraCyanBright else AuraCardBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color(0xFF0F172A) else AuraTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                "Permissions" -> PermissionsCenterSection(viewModel, context)
                "Voice & Wake" -> VoiceAndWakeSection(viewModel, uiState)
                "Background & Battery" -> BackgroundAndBatterySection(viewModel, uiState, context)
                "Privacy & Security" -> PrivacyAndSecuritySection(viewModel, onClearDataClick = { showClearDataDialog = true })
                "Help & About" -> HelpAndAboutSection()
            }
        }

        // Clear Data Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text("Clear All Assistant Data?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "This will permanently delete your voice command history and reset all stored cache. Android permissions will remain as configured in system settings.",
                        color = AuraTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearHistory()
                            showClearDataDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete All Data", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showClearDataDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun PermissionsCenterSection(viewModel: AuraViewModel, context: Context) {
    val permissions = viewModel.getPermissionsList()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCyan.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = AuraCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "MAX only executes actions when explicitly authorized by standard Android system permissions. No secret access is ever performed.",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(permissions) { perm ->
            PermissionItemCard(
                perm = perm,
                onOpenSettings = {
                    when (perm.permissionKey) {
                        "ACCESSIBILITY" -> {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                        "NOTIFICATION_LISTENER" -> {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                        else -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionItemCard(
    perm: PermissionStatus,
    onOpenSettings: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AuraCardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (perm.isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Status",
                        tint = if (perm.isGranted) AuraNeonGreen else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = perm.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (perm.isGranted) AuraNeonGreen.copy(alpha = 0.15f)
                                else Color(0xFFF59E0B).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (perm.isGranted) "Granted" else "Missing",
                            color = if (perm.isGranted) AuraNeonGreen else Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = perm.description,
                    color = AuraTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (perm.isGranted) AuraCardGlassHigh else AuraCyanBright
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .border(
                        1.dp,
                        if (perm.isGranted) AuraCardBorder else AuraCyanBright,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    text = if (perm.isGranted) "Manage" else "Enable",
                    color = if (perm.isGranted) AuraTextSecondary else Color(0xFF0F172A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VoiceAndWakeSection(viewModel: AuraViewModel, uiState: AuraUiState) {
    var sensitivity by remember { mutableFloatStateOf(0.8f) }
    var selectedLang by remember { mutableStateOf("hi-IN (Hindi / Hinglish)") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WAKE WORD DETECTION",
                        color = AuraCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Wake Phrase: \"MAX\" / \"Hey MAX\"", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Listens for 'MAX', 'Hey MAX', and 'MAX suno'", color = AuraTextMuted, fontSize = 12.sp)
                        }

                        Switch(
                            checked = uiState.isWakeWordActive,
                            onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0F172A), checkedTrackColor = AuraCyanBright)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sensitivity: ${(sensitivity * 100).toInt()}%",
                        color = AuraTextSecondary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraCyanBright,
                            activeTrackColor = AuraCyanBright,
                            inactiveTrackColor = AuraCardBorder
                        )
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONTINUOUS CONVERSATION",
                        color = AuraNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Follow-up Listening Mode", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Keep mic open for follow-up commands like 'Ab Shah Rukh Khan search karo'", color = AuraTextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = uiState.isContinuousListening,
                            onCheckedChange = { viewModel.setContinuousListening(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0F172A), checkedTrackColor = AuraNeonGreen)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VOICE OUTPUT & LANGUAGE",
                        color = AuraIndigo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Spoken Identity:", color = AuraTextMuted, fontSize = 12.sp)
                    Text(text = "\"Ji, main MAX hoon. Boliye.\"", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.replayAssistantVoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraCyan.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test Voice", tint = AuraCyanBright, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice Audio", color = AuraCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BackgroundAndBatterySection(viewModel: AuraViewModel, uiState: AuraUiState, context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else true

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = "Foreground Assistant Service", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Keeps MAX ready in background for wake words", color = AuraTextMuted, fontSize = 12.sp)
                        }

                        Switch(
                            checked = uiState.isForegroundServiceActive,
                            onCheckedChange = { viewModel.toggleForegroundService(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0F172A), checkedTrackColor = AuraCyanBright)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = "Battery", tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Battery Optimization Guidance", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Android OS may sleep background apps unless 'Unrestricted' battery usage is enabled. This is standard Android behavior designed to save battery.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(text = "Status: ${if (isIgnoringBatteryOptimizations) "🟢 Unrestricted Battery (Ideal)" else "🟡 Battery Optimized (May be paused)"}", color = if (isIgnoringBatteryOptimizations) AuraNeonGreen else Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "To allow reliable background wake-word detection, tap below to open app settings and choose 'Unrestricted' battery.", color = AuraTextMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(fallback)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Battery Optimization Settings", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrivacyAndSecuritySection(viewModel: AuraViewModel, onClearDataClick: () -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAFETY CONFIRMATION TIERS",
                        color = AuraCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SafetyTierRow(level = "Level 1: Safe", desc = "Open App, Volume, Navigation, Web Search, Timer — Instant execution without confirmation", tint = AuraNeonGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    SafetyTierRow(level = "Level 2: Important", desc = "Phone Calls, SMS, WhatsApp Messages — Voice confirmation prompt before sending", tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.height(8.dp))
                    SafetyTierRow(level = "Level 3: High Risk", desc = "File Deletion, System Resets — Explicit spoken confirmation required", tint = Color(0xFFEF4444))
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRIVACY ASSURANCE",
                        color = AuraNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• No Hidden Recording: Microphone is only triggered upon 'MAX' / 'Hey MAX' wake word or manual mic tap.\n• Lock Screen Rules: Sensitive operations respect phone lock screen.\n• Local SQLite Database: Command history is saved locally on device.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Reset Assistant Data", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Permanently clear voice logs and command history from local device storage.", color = AuraTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onClearDataClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear All Voice Data", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SafetyTierRow(level: String, desc: String, tint: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Column {
            Text(text = level, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = AuraTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun HelpAndAboutSection() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GETTING STARTED GUIDE",
                        color = AuraCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Say 'MAX' or 'Hey MAX' anytime to wake up the assistant.\n2. Speak natural commands in Hindi, Hinglish or English (e.g. 'MAX, call receive karo', 'MAX, call reject karo', 'WhatsApp kholo', 'WiFi on kar', 'Papa ko call lagao').\n3. MAX will reply with short, crisp voice confirmation: 'Ji, YouTube khol raha hoon.'\n4. For hands-free background activation, keep the Foreground Service enabled and allow Unrestricted Battery.",
                        color = AuraTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraCardBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ABOUT MAX",
                        color = AuraNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "App Name: MAX Voice Assistant", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Version: 2.0 (Build 2026.08)", color = AuraTextMuted, fontSize = 12.sp)
                    Text(text = "Architecture: Jetpack Compose + M3 + Foreground Services + Call Controls + Accessibility", color = AuraTextMuted, fontSize = 12.sp)
                    Text(text = "Privacy: Zero secret recording, on-device SQLite, permission-gated APIs", color = AuraTextMuted, fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
