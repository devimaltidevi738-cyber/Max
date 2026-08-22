package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuraDatabase
import com.example.data.local.entities.AutomationRoutineEntity
import com.example.data.local.entities.CommandHistoryEntity
import com.example.data.local.entities.ContactAliasEntity
import com.example.data.local.entities.CustomShortcutEntity
import com.example.engine.AuraActionExecutor
import com.example.engine.AuraNluParser
import com.example.engine.AuraVoiceEngine
import com.example.model.CommandIntent
import com.example.model.ConfirmationLevel
import com.example.model.ExecutionResult
import com.example.model.PendingConfirmation
import com.example.model.SpeechState
import com.example.services.AdiForegroundService
import com.example.services.AuraAccessibilityService
import com.example.services.AuraNotificationListener
import com.example.telephony.ActiveCallData
import com.example.telephony.AuraCallManager
import com.example.telephony.CallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BackgroundServiceStatus {
    ACTIVE,             // 🟢 Active
    PERMISSION_REQUIRED,// 🟡 Permission Required
    STOPPED             // 🔴 Stopped
}

data class PermissionStatus(
    val name: String,
    val permissionKey: String,
    val isGranted: Boolean,
    val description: String,
    val category: String
)

data class AuraUiState(
    val speechState: SpeechState = SpeechState.IDLE,
    val rmsAmplitude: Float = 0f,
    val liveTranscript: String = "Tap mic or say 'Hey MAX'",
    val assistantResponse: String = "Namaste! Main MAX hoon. Boliye.",
    val lastResult: ExecutionResult? = null,
    val isContinuousListening: Boolean = false,
    val pendingConfirmation: PendingConfirmation? = null,
    val activeContextSession: String? = null,
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val isForegroundServiceActive: Boolean = false,
    val backgroundServiceStatus: BackgroundServiceStatus = BackgroundServiceStatus.ACTIVE,
    val isWakeWordActive: Boolean = true,
    val wakeWordPhrase: String = "Hey MAX",
    val avatarType: String = "DEFAULT_MAX", // "DEFAULT_MAX", "AI_AVATAR", "CUSTOM_USER"
    val grantedPermissionsCount: Int = 0,
    val totalPermissionsCount: Int = 10
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val database = AuraDatabase.getDatabase(context)
    private val nluParser = AuraNluParser()
    private val actionExecutor = AuraActionExecutor(context)
    val callManager = AuraCallManager.getInstance(context)

    private val _uiState = MutableStateFlow(AuraUiState())
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    val callState: StateFlow<ActiveCallData> = callManager.callState

    val commandHistory: StateFlow<List<CommandHistoryEntity>> = database.commandHistoryDao()
        .getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactAliases: StateFlow<List<ContactAliasEntity>> = database.contactAliasDao()
        .getAllAliases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customShortcuts: StateFlow<List<CustomShortcutEntity>> = database.customShortcutDao()
        .getAllShortcuts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationRoutines: StateFlow<List<AutomationRoutineEntity>> = database.automationRoutineDao()
        .getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceEngine = AuraVoiceEngine(
        context = context,
        onCommandRecognized = { command ->
            processCommand(command)
        },
        onWakeWordDetected = {
            _uiState.value = _uiState.value.copy(
                liveTranscript = "Hey MAX",
                assistantResponse = "Ji, main MAX hoon. Boliye."
            )
        }
    )

    init {
        viewModelScope.launch {
            voiceEngine.speechState.collect { state ->
                _uiState.value = _uiState.value.copy(speechState = state)
            }
        }
        viewModelScope.launch {
            voiceEngine.rmsAmplitude.collect { amp ->
                _uiState.value = _uiState.value.copy(rmsAmplitude = amp)
            }
        }
        viewModelScope.launch {
            voiceEngine.liveTranscript.collect { transcript ->
                if (transcript.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(liveTranscript = transcript)
                }
            }
        }
        viewModelScope.launch {
            AuraAccessibilityService.isServiceActive.collect { active ->
                _uiState.value = _uiState.value.copy(isAccessibilityEnabled = active)
                refreshSystemStatus()
            }
        }
        viewModelScope.launch {
            AuraNotificationListener.isListenerActive.collect { active ->
                _uiState.value = _uiState.value.copy(isNotificationListenerEnabled = active)
                refreshSystemStatus()
            }
        }
        viewModelScope.launch {
            AdiForegroundService.isServiceRunning.collect { active ->
                _uiState.value = _uiState.value.copy(isForegroundServiceActive = active)
            }
        }

        refreshSystemStatus()
        // Auto-start background assistant foreground service
        try {
            AdiForegroundService.start(context)
        } catch (e: Exception) {
            // Ignore if in test env
        }
    }

    fun refreshSystemStatus() {
        val permissions = getPermissionsList()
        val grantedCount = permissions.count { it.isGranted }
        val isServiceActive = AdiForegroundService.isServiceRunning.value
        val micGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        val status = when {
            !micGranted -> BackgroundServiceStatus.PERMISSION_REQUIRED
            isServiceActive -> BackgroundServiceStatus.ACTIVE
            else -> BackgroundServiceStatus.STOPPED
        }

        _uiState.value = _uiState.value.copy(
            isAccessibilityEnabled = AuraAccessibilityService.isConnected(),
            isNotificationListenerEnabled = AuraNotificationListener.isConnected(),
            isForegroundServiceActive = isServiceActive,
            backgroundServiceStatus = status,
            grantedPermissionsCount = grantedCount,
            totalPermissionsCount = permissions.size
        )
    }

    fun startBackgroundService() {
        AdiForegroundService.start(context)
        refreshSystemStatus()
    }

    fun stopBackgroundService() {
        AdiForegroundService.stop(context)
        refreshSystemStatus()
    }

    fun restartBackgroundService() {
        AdiForegroundService.stop(context)
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            AdiForegroundService.start(context)
            refreshSystemStatus()
        }
    }

    fun getPermissionsList(): List<PermissionStatus> {
        val micGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val phoneGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val answerCallsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
        } else true
        val phoneStateGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val contactsGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val smsGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val cameraGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val locationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val notifPostGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        val accessServiceGranted = AuraAccessibilityService.isConnected()
        val notifListenerGranted = AuraNotificationListener.isConnected()

        return listOf(
            PermissionStatus("Microphone", android.Manifest.permission.RECORD_AUDIO, micGranted, "Required for hands-free 'Hey MAX' wake word and speech recognition.", "Core Voice"),
            PermissionStatus("Answer Phone Calls", android.Manifest.permission.ANSWER_PHONE_CALLS, answerCallsGranted, "Allows MAX to answer/reject incoming calls on voice command.", "Call Control"),
            PermissionStatus("Phone State", android.Manifest.permission.READ_PHONE_STATE, phoneStateGranted, "Allows MAX to detect incoming caller details in real-time.", "Call Control"),
            PermissionStatus("Phone Calls", android.Manifest.permission.CALL_PHONE, phoneGranted, "Enables direct hands-free calling to your contacts.", "Communication"),
            PermissionStatus("Contacts", android.Manifest.permission.READ_CONTACTS, contactsGranted, "Matches spoken names like 'Papa' or 'Rahul' to phone numbers.", "Communication"),
            PermissionStatus("Accessibility Service", "ACCESSIBILITY", accessServiceGranted, "Enables voice controls for Home, Back, Recents, Lock Screen, and scrolling.", "System Control"),
            PermissionStatus("Notification Listener", "NOTIFICATION_LISTENER", notifListenerGranted, "Allows MAX to read unread notifications and WhatsApp updates aloud.", "Notifications"),
            PermissionStatus("SMS Messages", android.Manifest.permission.SEND_SMS, smsGranted, "Allows MAX to send SMS text messages by voice.", "Communication"),
            PermissionStatus("Camera", android.Manifest.permission.CAMERA, cameraGranted, "Enables voice commands to open Camera and capture photos/videos.", "Media & Camera"),
            PermissionStatus("Location / GPS", android.Manifest.permission.ACCESS_FINE_LOCATION, locationGranted, "Provides navigation routes and nearby search in Google Maps.", "Navigation"),
            PermissionStatus("Post Notifications", android.Manifest.permission.POST_NOTIFICATIONS, notifPostGranted, "Shows persistent background status and wake word readiness.", "Background Service")
        )
    }

    fun answerCall() {
        val result = callManager.answerCall()
        _uiState.value = _uiState.value.copy(
            assistantResponse = result.message,
            lastResult = result
        )
        voiceEngine.speak(result.message, isHindi = true)
    }

    fun rejectCall() {
        val result = callManager.rejectCall()
        _uiState.value = _uiState.value.copy(
            assistantResponse = result.message,
            lastResult = result
        )
        voiceEngine.speak(result.message, isHindi = true)
    }

    fun endCall() {
        val result = callManager.endCall()
        _uiState.value = _uiState.value.copy(
            assistantResponse = result.message,
            lastResult = result
        )
        voiceEngine.speak(result.message, isHindi = true)
    }

    fun toggleSpeakerphone() {
        val currentSpeaker = callState.value.isSpeakerOn
        val result = callManager.setSpeakerphone(!currentSpeaker)
        _uiState.value = _uiState.value.copy(
            assistantResponse = result.message,
            lastResult = result
        )
        voiceEngine.speak(result.message, isHindi = true)
    }

    fun simulateIncomingCall(callerName: String = "Papa", callerNumber: String = "+91 98765 43210") {
        callManager.simulateIncomingCall(callerName, callerNumber)
        val prompt = "$callerName se call aa raha hai."
        _uiState.value = _uiState.value.copy(
            assistantResponse = prompt,
            liveTranscript = "Incoming Call: $callerName"
        )
        voiceEngine.speak(prompt, isHindi = true)
    }

    fun dismissCallState() {
        callManager.dismissCallState()
    }

    fun toggleForegroundService(enable: Boolean) {
        if (enable) {
            AdiForegroundService.start(context)
        } else {
            AdiForegroundService.stop(context)
        }
        _uiState.value = _uiState.value.copy(isForegroundServiceActive = enable)
    }

    fun setAvatarType(type: String) {
        _uiState.value = _uiState.value.copy(avatarType = type)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isWakeWordActive = enabled)
    }

    fun setWakeWordPhrase(phrase: String) {
        _uiState.value = _uiState.value.copy(wakeWordPhrase = phrase)
    }

    fun toggleListening() {
        voiceEngine.toggleListening()
    }

    fun setContinuousListening(enabled: Boolean) {
        voiceEngine.setContinuousListening(enabled)
        _uiState.value = _uiState.value.copy(isContinuousListening = enabled)
    }

    fun processCommand(rawInput: String) {
        if (rawInput.isBlank()) return

        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                liveTranscript = rawInput,
                speechState = SpeechState.PROCESSING
            )

            // 1. Check custom user shortcuts first
            val shortcut = database.customShortcutDao().findShortcut(rawInput.trim())
            val effectiveQuery = shortcut?.targetCommand ?: rawInput

            // 2. Parse NLU intent
            val intent = nluParser.parse(effectiveQuery)
            _uiState.value = _uiState.value.copy(
                activeContextSession = nluParser.getContext().lastOpenedApp
            )

            // 3. Handle Confirmation Flow for Level 2 & 3
            if (intent.confirmationLevel != ConfirmationLevel.LEVEL_1_SAFE && intent.actionName != "CONFIRMED_EXECUTION" && intent.actionName != "CANCELLED") {
                val prompt = intent.spokenResponseHindi
                _uiState.value = _uiState.value.copy(
                    assistantResponse = prompt,
                    pendingConfirmation = PendingConfirmation(intent, prompt)
                )
                voiceEngine.speak(prompt, isHindi = true)
                return@launch
            }

            // 4. Handle Cancelled action
            if (intent.actionName == "CANCELLED") {
                _uiState.value = _uiState.value.copy(
                    assistantResponse = intent.spokenResponseHindi,
                    pendingConfirmation = null
                )
                voiceEngine.speak(intent.spokenResponseHindi, isHindi = true)
                return@launch
            }

            // 5. Execute Action
            _uiState.value = _uiState.value.copy(pendingConfirmation = null)

            val result: ExecutionResult
            if (intent.isMultiStep && intent.subIntents.isNotEmpty()) {
                var allSuccess = true
                val messages = mutableListOf<String>()
                for (subIntent in intent.subIntents) {
                    val subRes = actionExecutor.execute(subIntent)
                    messages.add(subRes.message)
                    if (!subRes.isSuccess) {
                        allSuccess = false
                    }
                }
                result = ExecutionResult(
                    isSuccess = allSuccess,
                    actionName = "MULTI_STEP",
                    message = messages.joinToString(" aur ")
                )
            } else {
                result = actionExecutor.execute(intent)
            }

            val latency = System.currentTimeMillis() - startTime
            val spokenMessage = if (result.isSuccess) {
                if (intent.spokenResponseHindi.isNotBlank()) intent.spokenResponseHindi else result.message
            } else {
                result.errorReason ?: result.message
            }

            _uiState.value = _uiState.value.copy(
                assistantResponse = spokenMessage,
                lastResult = result,
                speechState = SpeechState.IDLE
            )

            voiceEngine.speak(spokenMessage, isHindi = true)

            database.commandHistoryDao().insertHistory(
                CommandHistoryEntity(
                    rawQuery = rawInput,
                    recognizedLanguage = detectLanguageLabel(rawInput),
                    category = intent.category.name,
                    actionName = intent.actionName,
                    responseText = spokenMessage,
                    isSuccess = result.isSuccess,
                    latencyMs = latency,
                    details = result.details
                )
            )
        }
    }

    fun executeRoutine(routine: AutomationRoutineEntity) {
        val lines = routine.commandsSequence.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val composite = lines.joinToString(" aur ")
        processCommand(composite)
    }

    fun addRoutine(title: String, time: String, desc: String, commands: String, iconCategory: String) {
        viewModelScope.launch {
            database.automationRoutineDao().insertRoutine(
                AutomationRoutineEntity(
                    title = title.trim(),
                    triggerTime = time.trim(),
                    description = desc.trim(),
                    commandsSequence = commands.trim(),
                    isEnabled = true,
                    iconCategory = iconCategory
                )
            )
        }
    }

    fun toggleRoutine(routine: AutomationRoutineEntity) {
        viewModelScope.launch {
            database.automationRoutineDao().updateRoutine(
                routine.copy(isEnabled = !routine.isEnabled)
            )
        }
    }

    fun deleteRoutine(routine: AutomationRoutineEntity) {
        viewModelScope.launch {
            database.automationRoutineDao().deleteRoutine(routine)
        }
    }

    fun confirmPendingAction(confirmed: Boolean) {
        val pending = _uiState.value.pendingConfirmation ?: return
        if (confirmed) {
            processCommand("haan")
        } else {
            processCommand("nahi")
        }
    }

    fun replayAssistantVoice() {
        val text = _uiState.value.assistantResponse
        if (text.isNotBlank()) {
            voiceEngine.speak(text, isHindi = true)
        }
    }

    fun addContactAlias(aliasName: String, contactName: String, phone: String) {
        viewModelScope.launch {
            database.contactAliasDao().insertAlias(
                ContactAliasEntity(
                    aliasName = aliasName.trim(),
                    actualContactName = contactName.trim(),
                    phoneNumber = phone.trim()
                )
            )
        }
    }

    fun deleteContactAlias(alias: ContactAliasEntity) {
        viewModelScope.launch {
            database.contactAliasDao().deleteAlias(alias)
        }
    }

    fun addShortcut(phrase: String, desc: String, targetCommand: String) {
        viewModelScope.launch {
            database.customShortcutDao().insertShortcut(
                CustomShortcutEntity(
                    triggerPhrase = phrase.trim(),
                    actionDescription = desc.trim(),
                    targetCommand = targetCommand.trim(),
                    isEnabled = true
                )
            )
        }
    }

    fun deleteShortcut(shortcut: CustomShortcutEntity) {
        viewModelScope.launch {
            database.customShortcutDao().deleteShortcut(shortcut)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            database.commandHistoryDao().clearAllHistory()
        }
    }

    private fun detectLanguageLabel(text: String): String {
        val hindiChars = text.any { it.code in 0x0900..0x097F }
        return if (hindiChars) "Hindi"
        else if (text.contains("kholo", ignoreCase = true) || text.contains("karo", ignoreCase = true) || text.contains("bhejo", ignoreCase = true) || text.contains("jao", ignoreCase = true) || text.contains("laga", ignoreCase = true)) "Hinglish"
        else "English"
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
