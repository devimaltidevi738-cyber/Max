package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.AuraBackgroundDark
import com.example.ui.theme.AuraCardBorder
import com.example.ui.theme.AuraCardGlassHigh
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanBright
import com.example.ui.theme.AuraNeonGreen
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextSecondary
import com.example.viewmodel.AuraViewModel

data class CommandTemplate(
    val title: String,
    val hindiPhrase: String,
    val englishPhrase: String,
    val category: String,
    val icon: ImageVector,
    val isImportant: Boolean = false,
    val requiresAccessibility: Boolean = false
)

@Composable
fun CommandDeckScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Navigation", "Apps", "Calls & SMS", "Settings", "Media & Sound", "Alarms", "Search & Maps")

    val allCommands = remember {
        listOf(
            CommandTemplate("Go to Home", "Home pe jao / Home screen", "Go to home screen", "Navigation", Icons.Default.Home, requiresAccessibility = true),
            CommandTemplate("Go Back", "Back karo / Peeche jao", "Go back", "Navigation", Icons.AutoMirrored.Filled.ArrowBack, requiresAccessibility = true),
            CommandTemplate("Recent Apps", "Recent apps kholo", "Open recent apps", "Navigation", Icons.Default.Apps, requiresAccessibility = true),
            CommandTemplate("Open Notifications", "Notifications kholo", "Open notification shade", "Navigation", Icons.Default.Notifications, requiresAccessibility = true),
            CommandTemplate("Lock Screen", "Screen lock karo / Phone lock karo", "Lock the screen", "Navigation", Icons.Default.Lock, requiresAccessibility = true),

            CommandTemplate("Open YouTube", "YouTube kholo / Chrome me YouTube kholo", "Open YouTube", "Apps", Icons.Default.PlayArrow),
            CommandTemplate("Open WhatsApp", "WhatsApp kholo", "Open WhatsApp", "Apps", Icons.Default.Smartphone),
            CommandTemplate("Open Camera", "Camera kholo / Video camera kholo", "Open camera", "Apps", Icons.Default.CameraAlt),
            CommandTemplate("Open Downloads", "Downloads folder kholo", "Open downloads", "Apps", Icons.Default.Download),

            CommandTemplate("Answer Call", "MAX, call receive karo / Call utha lo", "Answer incoming call", "Calls & SMS", Icons.Default.Call),
            CommandTemplate("Reject Call", "MAX, call reject karo / Call decline karo", "Reject incoming call", "Calls & SMS", Icons.Default.CallEnd),
            CommandTemplate("End / Cut Call", "Call kaat do / Call disconnect karo", "Hang up active call", "Calls & SMS", Icons.Default.CallEnd),
            CommandTemplate("Speakerphone ON", "Speaker on karo / Loudspeaker on", "Enable speakerphone", "Calls & SMS", Icons.AutoMirrored.Filled.VolumeUp),
            CommandTemplate("Speakerphone OFF", "Speaker off karo / Loudspeaker off", "Disable speakerphone", "Calls & SMS", Icons.AutoMirrored.Filled.VolumeUp),
            CommandTemplate("Direct Call", "Papa ko call laga do", "Call Papa", "Calls & SMS", Icons.Default.Call, isImportant = true),
            CommandTemplate("Send WhatsApp Msg", "WhatsApp par Rahul ko message bhejo", "Send WhatsApp message", "Calls & SMS", Icons.Default.Smartphone, isImportant = true),
            CommandTemplate("Send SMS", "Rahul ko message bhejo: 'Kal milte hain'", "Send SMS to contact", "Calls & SMS", Icons.Default.Call, isImportant = true),
            CommandTemplate("Read Notifications", "Notifications padho / WhatsApp notification batao", "Read unread notifications", "Calls & SMS", Icons.Default.Notifications),

            CommandTemplate("WiFi Settings", "WiFi on kar / WiFi settings kholo", "Open WiFi settings", "Settings", Icons.Default.Wifi),
            CommandTemplate("Bluetooth Settings", "Bluetooth band karo / Bluetooth settings", "Open Bluetooth settings", "Settings", Icons.Default.Settings),
            CommandTemplate("Airplane Mode", "Airplane mode settings kholo", "Open Airplane mode", "Settings", Icons.Default.Settings),
            CommandTemplate("Display Settings", "Display settings kholo / Brightness", "Open Display settings", "Settings", Icons.Default.Settings),

            CommandTemplate("Set Volume", "Volume 50 percent kar do", "Set volume to 50%", "Media & Sound", Icons.AutoMirrored.Filled.VolumeUp),
            CommandTemplate("Silent Mode", "Silent mode on / Phone silent karo", "Turn on Silent mode", "Media & Sound", Icons.AutoMirrored.Filled.VolumeUp),
            CommandTemplate("Vibrate Mode", "Vibrate mode on karo", "Turn on Vibrate mode", "Media & Sound", Icons.AutoMirrored.Filled.VolumeUp),

            CommandTemplate("Set Alarm", "Alarm kal subah 7 baje ka laga do", "Set alarm for 7:00 AM", "Alarms", Icons.Default.Alarm),
            CommandTemplate("Set Timer", "10 minute ka timer shuru karo", "Set a 10-minute timer", "Alarms", Icons.Default.Alarm),
            CommandTemplate("Show Alarms", "Saare alarms dikhao", "Show all alarms", "Alarms", Icons.Default.Alarm),

            CommandTemplate("Google Search", "Google par latest news search karo", "Search on Google", "Search & Maps", Icons.Default.Search),
            CommandTemplate("Search YouTube", "YouTube par top songs search karo", "Search YouTube", "Search & Maps", Icons.Default.Search),
            CommandTemplate("Directions & Maps", "Nearest hospital ka route dikhao", "Navigate to hospital", "Search & Maps", Icons.Default.Map)
        )
    }

    val filteredCommands = allCommands.filter { cmd ->
        (selectedCategory == "All" || cmd.category == selectedCategory) &&
                (searchQuery.isBlank() || cmd.title.contains(searchQuery, ignoreCase = true) ||
                        cmd.hindiPhrase.contains(searchQuery, ignoreCase = true) ||
                        cmd.englishPhrase.contains(searchQuery, ignoreCase = true))
    }

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
                text = "Command Deck",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "Spoken Hindi, Hinglish & English command triggers",
                color = AuraTextMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search voice commands...", color = AuraTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AuraCyan
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraCyanBright,
                    unfocusedBorderColor = AuraCardBorder,
                    focusedContainerColor = AuraCardGlassHigh,
                    unfocusedContainerColor = AuraCardGlassHigh,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("command_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in categories) {
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AuraCyanBright else AuraCardGlassHigh)
                            .border(
                                1.dp,
                                if (isSelected) AuraCyanBright else AuraCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color(0xFF0F172A) else AuraTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Commands List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCommands) { cmd ->
                    CommandItemCard(
                        template = cmd,
                        onTestRun = { viewModel.processCommand(cmd.hindiPhrase.split("/")[0].trim()) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CommandItemCard(
    template: CommandTemplate,
    onTestRun: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AuraCardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(AuraCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = template.icon,
                            contentDescription = template.title,
                            tint = AuraCyanBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = template.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = template.category,
                            color = AuraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Run Now Button
                Button(
                    onClick = onTestRun,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyan.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test",
                            tint = AuraCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Test",
                            color = AuraCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hindi / Hinglish trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🗣️ \"${template.hindiPhrase}\"",
                    color = Color(0xFF67E8F9),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (template.requiresAccessibility) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Accessibility Required",
                            color = Color(0xFFA5B4FC),
                            fontSize = 10.sp
                        )
                    }
                }
                if (template.isImportant) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Confirmation Protected",
                            color = Color(0xFFFCD34D),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
