package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val rawQuery: String,
    val recognizedLanguage: String, // "Hinglish", "Hindi", "English"
    val category: String,
    val actionName: String,
    val responseText: String,
    val isSuccess: Boolean,
    val latencyMs: Long = 0,
    val details: String? = null
)

@Entity(tableName = "contact_aliases")
data class ContactAliasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val aliasName: String, // e.g. "Papa", "Mummy", "Rahul", "Amit"
    val actualContactName: String,
    val phoneNumber: String,
    val preferredApp: String = "PHONE" // "PHONE" or "WHATSAPP"
)

@Entity(tableName = "custom_shortcuts")
data class CustomShortcutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerPhrase: String,
    val actionDescription: String,
    val targetCommand: String,
    val isEnabled: Boolean = true
)

@Entity(tableName = "automation_routines")
data class AutomationRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val triggerTime: String,
    val description: String,
    val commandsSequence: String, // comma or newline separated
    val isEnabled: Boolean = true,
    val iconCategory: String = "MORNING"
)
