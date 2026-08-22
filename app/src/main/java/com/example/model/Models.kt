package com.example.model

enum class SpeechState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

enum class ConfirmationLevel {
    LEVEL_1_SAFE,       // Safe actions: Open App, Volume, Navigation, Web Search, Timer
    LEVEL_2_IMPORTANT,  // Important: Making Calls, Sending Messages, WhatsApp message
    LEVEL_3_HIGH_RISK   // High Risk: Deleting files, Clear data, Factory resets
}

enum class ActionCategory {
    NAVIGATION,
    APPS,
    CALLS,
    MESSAGES,
    WHATSAPP,
    AUDIO_VOLUME,
    CONNECTIVITY_SETTINGS,
    ALARM_TIMER,
    REMINDER,
    MAPS_LOCATION,
    WEB_SEARCH,
    CAMERA,
    FILES,
    NOTIFICATIONS,
    SYSTEM_AUTOMATION,
    UNKNOWN
}

data class CommandIntent(
    val rawQuery: String,
    val category: ActionCategory,
    val actionName: String,
    val primaryParam: String? = null,
    val secondaryParam: String? = null,
    val numericValue: Int? = null,
    val targetPackage: String? = null,
    val confirmationLevel: ConfirmationLevel = ConfirmationLevel.LEVEL_1_SAFE,
    val spokenResponseHindi: String,
    val spokenResponseEnglish: String,
    val requiresAccessibility: Boolean = false,
    val isMultiStep: Boolean = false,
    val subIntents: List<CommandIntent> = emptyList()
)

data class ExecutionResult(
    val isSuccess: Boolean,
    val message: String,
    val actionName: String,
    val details: String? = null,
    val errorReason: String? = null
)

data class PendingConfirmation(
    val intent: CommandIntent,
    val confirmationPrompt: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
