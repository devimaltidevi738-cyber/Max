package com.example.telephony

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AuraDatabase
import com.example.model.ExecutionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    RINGING,
    OFFHOOK, // Call in progress
    DISCONNECTED
}

data class ActiveCallData(
    val state: CallState = CallState.IDLE,
    val callerName: String = "",
    val callerNumber: String = "",
    val isSpeakerOn: Boolean = false,
    val callDurationSeconds: Int = 0,
    val isSimulated: Boolean = false
)

class AuraCallManager private constructor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    private val database = AuraDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _callState = MutableStateFlow(ActiveCallData())
    val callState: StateFlow<ActiveCallData> = _callState.asStateFlow()

    private var durationJob: Job? = null
    private var isReceiverRegistered = false

    private val phoneStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val action = intent.action
            if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
                handleSystemPhoneState(stateStr, incomingNumber)
            }
        }
    }

    init {
        registerReceiver()
    }

    fun registerReceiver() {
        if (isReceiverRegistered) return
        try {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            context.registerReceiver(phoneStateReceiver, filter)
            isReceiverRegistered = true
            Log.d(TAG, "PhoneStateReceiver registered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register PhoneStateReceiver", e)
        }
    }

    fun unregisterReceiver() {
        if (!isReceiverRegistered) return
        try {
            context.unregisterReceiver(phoneStateReceiver)
            isReceiverRegistered = false
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    private fun handleSystemPhoneState(stateStr: String?, incomingNumber: String) {
        Log.d(TAG, "Phone state changed: $stateStr, number: $incomingNumber")
        when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                scope.launch {
                    val resolvedName = resolveCallerName(incomingNumber)
                    _callState.value = ActiveCallData(
                        state = CallState.RINGING,
                        callerName = resolvedName,
                        callerNumber = if (incomingNumber.isNotBlank()) incomingNumber else "Unknown Caller",
                        isSpeakerOn = audioManager.isSpeakerphoneOn,
                        callDurationSeconds = 0,
                        isSimulated = false
                    )
                }
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                val current = _callState.value
                _callState.value = current.copy(
                    state = CallState.OFFHOOK,
                    callerName = if (current.callerName.isNotBlank()) current.callerName else "Active Call",
                    callerNumber = current.callerNumber,
                    isSpeakerOn = audioManager.isSpeakerphoneOn
                )
                startDurationTimer()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                stopDurationTimer()
                _callState.value = ActiveCallData(
                    state = CallState.IDLE,
                    callerName = "",
                    callerNumber = "",
                    isSpeakerOn = false,
                    callDurationSeconds = 0
                )
            }
        }
    }

    suspend fun resolveCallerName(phoneNumber: String): String {
        if (phoneNumber.isBlank()) return "Unknown Caller"

        try {
            val alias = database.contactAliasDao().findByAlias(phoneNumber)
            if (alias != null && alias.actualContactName.isNotBlank()) {
                return alias.actualContactName
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contact alias", e)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                    .appendPath(phoneNumber).build()
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(0)
                        if (!name.isNullOrBlank()) return name
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Contact lookup failed", e)
            }
        }

        return if (phoneNumber.startsWith("+91")) "Caller ($phoneNumber)" else phoneNumber
    }

    fun answerCall(): ExecutionResult {
        val current = _callState.value
        val hasAnswerPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasAnswerPermission && !current.isSimulated) {
            return ExecutionResult(
                isSuccess = false,
                actionName = "ANSWER_CALL",
                message = "Call receive karne ke liye 'Answer Phone Calls' permission zaroori hai.",
                errorReason = "Permission ANSWER_PHONE_CALLS not granted"
            )
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && telecomManager != null) {
                telecomManager.acceptRingingCall()
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "TelecomManager acceptRingingCall security exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "TelecomManager acceptRingingCall failed", e)
        }

        _callState.value = current.copy(
            state = CallState.OFFHOOK,
            callerName = if (current.callerName.isNotBlank()) current.callerName else "Active Call",
            callDurationSeconds = 0
        )
        startDurationTimer()

        return ExecutionResult(
            isSuccess = true,
            actionName = "ANSWER_CALL",
            message = "Ji, call receive kar di."
        )
    }

    fun rejectCall(): ExecutionResult {
        val current = _callState.value
        val hasAnswerPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && telecomManager != null) {
                telecomManager.endCall()
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "TelecomManager endCall security exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "TelecomManager endCall failed", e)
        }

        stopDurationTimer()
        _callState.value = ActiveCallData(state = CallState.IDLE)

        return ExecutionResult(
            isSuccess = true,
            actionName = "REJECT_CALL",
            message = "Ji, call reject kar di."
        )
    }

    fun endCall(): ExecutionResult {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && telecomManager != null) {
                telecomManager.endCall()
            }
        } catch (e: Exception) {
            Log.e(TAG, "TelecomManager endCall error", e)
        }

        stopDurationTimer()
        audioManager.isSpeakerphoneOn = false
        _callState.value = ActiveCallData(state = CallState.IDLE)

        return ExecutionResult(
            isSuccess = true,
            actionName = "END_CALL",
            message = "Call disconnect kar di."
        )
    }

    fun setSpeakerphone(enable: Boolean): ExecutionResult {
        try {
            audioManager.isSpeakerphoneOn = enable
            val current = _callState.value
            _callState.value = current.copy(isSpeakerOn = enable)
            val msg = if (enable) "Speaker on kar diya." else "Speaker off kar diya."
            return ExecutionResult(
                isSuccess = true,
                actionName = if (enable) "SPEAKER_ON" else "SPEAKER_OFF",
                message = msg
            )
        } catch (e: Exception) {
            return ExecutionResult(
                isSuccess = false,
                actionName = if (enable) "SPEAKER_ON" else "SPEAKER_OFF",
                message = "Speaker control nahi ho paya.",
                errorReason = e.message
            )
        }
    }

    fun simulateIncomingCall(callerName: String = "Papa", callerNumber: String = "+91 98765 43210") {
        stopDurationTimer()
        _callState.value = ActiveCallData(
            state = CallState.RINGING,
            callerName = callerName,
            callerNumber = callerNumber,
            isSpeakerOn = audioManager.isSpeakerphoneOn,
            callDurationSeconds = 0,
            isSimulated = true
        )
    }

    fun dismissCallState() {
        stopDurationTimer()
        _callState.value = ActiveCallData(state = CallState.IDLE)
    }

    private fun startDurationTimer() {
        stopDurationTimer()
        durationJob = scope.launch {
            while (isActive && _callState.value.state == CallState.OFFHOOK) {
                delay(1000)
                val current = _callState.value
                if (current.state == CallState.OFFHOOK) {
                    _callState.value = current.copy(callDurationSeconds = current.callDurationSeconds + 1)
                }
            }
        }
    }

    private fun stopDurationTimer() {
        durationJob?.cancel()
        durationJob = null
    }

    companion object {
        private const val TAG = "AuraCallManager"

        @Volatile
        private var instance: AuraCallManager? = null

        fun getInstance(context: Context): AuraCallManager {
            return instance ?: synchronized(this) {
                instance ?: AuraCallManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
