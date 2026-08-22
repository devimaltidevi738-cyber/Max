package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AutomationRoutineEntity
import com.example.data.local.entities.CustomShortcutEntity
import com.example.ui.theme.AuraBackgroundDark
import com.example.ui.theme.AuraCardBorder
import com.example.ui.theme.AuraCardGlassHigh
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanBright
import com.example.ui.theme.AuraNeonGreen
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextSecondary
import com.example.viewmodel.AuraViewModel

@Composable
fun ShortcutsScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.automationRoutines.collectAsState()
    val shortcuts by viewModel.customShortcuts.collectAsState()

    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAddShortcutDialog by remember { mutableStateOf(false) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Automations & Routines",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Automate multi-step tasks and voice shortcuts",
                        color = AuraTextMuted,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { showAddRoutineDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Routines Section
                item {
                    Text(
                        text = "DAILY SMART ROUTINES",
                        color = AuraCyanBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(routines) { routine ->
                    AutomationRoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutine(routine) },
                        onRunNow = { viewModel.executeRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine) }
                    )
                }

                // Custom Voice Shortcuts Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CUSTOM VOICE SHORTCUTS",
                            color = AuraNeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "+ Add Shortcut",
                            color = AuraCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showAddShortcutDialog = true }
                        )
                    }
                }

                items(shortcuts) { shortcut ->
                    CustomShortcutCard(
                        shortcut = shortcut,
                        onRun = { viewModel.processCommand(shortcut.targetCommand) },
                        onDelete = { viewModel.deleteShortcut(shortcut) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Add Routine Dialog
        if (showAddRoutineDialog) {
            AddRoutineDialog(
                onDismiss = { showAddRoutineDialog = false },
                onAdd = { title, time, desc, commands, cat ->
                    viewModel.addRoutine(title, time, desc, commands, cat)
                    showAddRoutineDialog = false
                }
            )
        }

        // Add Shortcut Dialog
        if (showAddShortcutDialog) {
            AddShortcutDialog(
                onDismiss = { showAddShortcutDialog = false },
                onAdd = { phrase, desc, target ->
                    viewModel.addShortcut(phrase, desc, target)
                    showAddShortcutDialog = false
                }
            )
        }
    }
}

@Composable
fun AutomationRoutineCard(
    routine: AutomationRoutineEntity,
    onToggle: () -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (routine.iconCategory) {
        "MORNING" -> Icons.Default.WbSunny
        "WORK" -> Icons.Default.Work
        "NIGHT" -> Icons.Default.Bedtime
        else -> Icons.Default.AutoMode
    }

    val iconTint = when (routine.iconCategory) {
        "MORNING" -> Color(0xFFF59E0B)
        "WORK" -> Color(0xFF38BDF8)
        "NIGHT" -> Color(0xFFA78BFA)
        else -> AuraCyanBright
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = routine.title,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = routine.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Time: ${routine.triggerTime}",
                            color = AuraTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0F172A),
                        checkedTrackColor = AuraCyanBright,
                        uncheckedThumbColor = AuraTextMuted,
                        uncheckedTrackColor = AuraCardGlassHigh
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routine.description,
                color = AuraTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Sequence Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "EXECUTED STEPS:",
                        color = AuraCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = routine.commandsSequence.replace("\n", "  ➔  "),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRunNow,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Run Routine",
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomShortcutCard(
    shortcut: CustomShortcutEntity,
    onRun: () -> Unit,
    onDelete: () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AuraNeonGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Shortcut",
                        tint = AuraNeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "\"${shortcut.triggerPhrase}\"",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = shortcut.actionDescription,
                        color = AuraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraNeonGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Run",
                        color = AuraNeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, time: String, desc: String, commands: String, cat: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00 AM") }
    var desc by remember { mutableStateOf("") }
    var commands by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MORNING") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Automation Routine", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Title (e.g. Morning Focus)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Trigger Time (e.g. 07:30 AM)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = commands,
                    onValueChange = { commands = it },
                    label = { Text("Commands (one per line)") },
                    placeholder = { Text("Volume 50 percent karo\nNotifications padho") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && commands.isNotBlank()) {
                        onAdd(title, time, desc, commands, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright)
            ) {
                Text("Create Routine", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

@Composable
fun AddShortcutDialog(
    onDismiss: () -> Unit,
    onAdd: (phrase: String, desc: String, target: String) -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetCommand by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Voice Shortcut", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phrase,
                    onValueChange = { phrase = it },
                    label = { Text("Trigger Phrase (e.g. Gym Mode)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = targetCommand,
                    onValueChange = { targetCommand = it },
                    label = { Text("Execute Command") },
                    placeholder = { Text("Volume 80 percent karo aur YouTube kholo") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phrase.isNotBlank() && targetCommand.isNotBlank()) {
                        onAdd(phrase, desc, targetCommand)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AuraCyanBright)
            ) {
                Text("Save Shortcut", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
