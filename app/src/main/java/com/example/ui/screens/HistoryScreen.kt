package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CommandHistoryEntity
import com.example.ui.theme.AuraBorder
import com.example.ui.theme.AuraCardDark
import com.example.ui.theme.AuraCardGlassHigh
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDeepSpace
import com.example.ui.theme.AuraGlassBorder
import com.example.ui.theme.AuraGreen
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraRed
import com.example.ui.theme.AuraSurfaceDark
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraTextTertiary
import com.example.ui.theme.AuraViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<CommandHistoryEntity>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDeepSpace)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Activity Log",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
                Text(
                    text = "Logged commands & execution latency",
                    fontSize = 13.sp,
                    color = AuraTextSecondary
                )
            }

            if (historyList.isNotEmpty()) {
                Button(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCardDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        tint = AuraRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Clear", color = AuraRed, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AuraTextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Abhi tak koi voice commands log nahi hain",
                        color = AuraTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Speak a command or tap on the Voice Hub to start",
                        color = AuraTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraCardGlassHigh),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (item.isSuccess) AuraGlassBorder else AuraRed.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (item.isSuccess) AuraGreen else AuraRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = item.actionName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTextPrimary
                                    )
                                }

                                Text(
                                    text = dateFormat.format(Date(item.timestamp)),
                                    fontSize = 11.sp,
                                    color = AuraTextTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "\"${item.rawQuery}\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraCyan
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "AURA: ${item.responseText}",
                                fontSize = 12.sp,
                                color = AuraTextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0x14FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraGlassBorder)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AuraIndigo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0x14FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraGlassBorder)
                                ) {
                                    Text(
                                        text = item.recognizedLanguage,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AuraTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0x14FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraGlassBorder)
                                ) {
                                    Text(
                                        text = "${item.latencyMs}ms",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AuraTextTertiary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
