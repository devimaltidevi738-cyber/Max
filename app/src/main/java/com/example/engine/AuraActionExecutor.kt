package com.example.engine

import android.app.DownloadManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AuraDatabase
import com.example.model.CommandIntent
import com.example.model.ExecutionResult
import com.example.services.AuraAccessibilityService
import com.example.services.AuraNotificationListener
import com.example.telephony.AuraCallManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuraActionExecutor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val database = AuraDatabase.getDatabase(context)

    suspend fun execute(intent: CommandIntent): ExecutionResult = withContext(Dispatchers.IO) {
        val action = intent.actionName
        Log.d(TAG, "Executing action: $action with intent: $intent")

        try {
            when (action) {
                // Navigation Actions
                "NAV_HOME" -> executeHome()
                "NAV_BACK" -> executeBack()
                "NAV_RECENTS" -> executeRecents()
                "NAV_NOTIFICATIONS" -> executeOpenNotifications()
                "NAV_QUICK_SETTINGS" -> executeQuickSettings()
                "NAV_LOCK_SCREEN" -> executeLockScreen()
                "NAV_SCROLL_DOWN" -> executeScroll(true)
                "NAV_SCROLL_UP" -> executeScroll(false)

                // Apps
                "OPEN_APP" -> executeOpenApp(intent.primaryParam, intent.targetPackage)

                // Calls & Call Control
                "MAKE_CALL" -> executeCall(intent.primaryParam)
                "CALL_LAST_CALLER" -> executeCallLastCaller()
                "ANSWER_CALL" -> executeAnswerCall()
                "REJECT_CALL" -> executeRejectCall()
                "END_CALL" -> executeEndCall()
                "SPEAKER_ON" -> executeSpeaker(true)
                "SPEAKER_OFF" -> executeSpeaker(false)

                // Messages & WhatsApp
                "SEND_SMS" -> executeSendSms(intent.primaryParam, intent.secondaryParam)
                "SEND_WHATSAPP_MESSAGE" -> executeWhatsAppMessage(intent.primaryParam, intent.secondaryParam)

                // Alarms & Timers
                "SET_ALARM" -> executeSetAlarm(intent.primaryParam, intent.secondaryParam)
                "SET_TIMER" -> executeSetTimer(intent.numericValue ?: 5)
                "SHOW_ALARMS" -> executeShowAlarms()
                "DISMISS_ALARM" -> executeDismissAlarm()

                // Audio & Volume
                "VOLUME_UP" -> executeVolumeAdjust(AudioManager.ADJUST_RAISE)
                "VOLUME_DOWN" -> executeVolumeAdjust(AudioManager.ADJUST_LOWER)
                "SET_VOLUME_PERCENT" -> executeSetVolumePercent(intent.numericValue ?: 50)
                "SET_RINGER_SILENT" -> executeSetRingerMode(AudioManager.RINGER_MODE_SILENT)
                "SET_RINGER_VIBRATE" -> executeSetRingerMode(AudioManager.RINGER_MODE_VIBRATE)

                // Connectivity & Settings
                "OPEN_WIFI_SETTINGS" -> executeOpenSetting(Settings.ACTION_WIFI_SETTINGS, "WiFi Settings")
                "OPEN_BLUETOOTH_SETTINGS" -> executeOpenSetting(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
                "OPEN_AIRPLANE_SETTINGS" -> executeOpenSetting(Settings.ACTION_AIRPLANE_MODE_SETTINGS, "Airplane Mode Settings")
                "OPEN_NETWORK_SETTINGS" -> executeOpenSetting(Settings.ACTION_WIRELESS_SETTINGS, "Mobile Network Settings")
                "OPEN_HOTSPOT_SETTINGS" -> executeOpenSetting(Settings.ACTION_WIRELESS_SETTINGS, "Hotspot Settings")
                "OPEN_DISPLAY_SETTINGS" -> executeOpenSetting(Settings.ACTION_DISPLAY_SETTINGS, "Display Settings")
                "OPEN_SETTINGS" -> executeOpenSetting(Settings.ACTION_SETTINGS, "System Settings")

                // Camera
                "OPEN_CAMERA" -> executeCamera(false)
                "OPEN_VIDEO_CAMERA" -> executeCamera(true)

                // Maps
                "OPEN_MAPS" -> executeOpenMaps()
                "NAVIGATE_TO" -> executeNavigateTo(intent.primaryParam ?: "Nearby")

                // Web Search
                "SEARCH_GOOGLE" -> executeGoogleSearch(intent.primaryParam ?: "")
                "SEARCH_YOUTUBE" -> executeYouTubeSearch(intent.primaryParam ?: "")

                // Notifications
                "READ_NOTIFICATIONS" -> executeReadNotifications(intent.primaryParam)

                // Files
                "OPEN_DOWNLOADS" -> executeOpenDownloads()
                "DELETE_FILE" -> ExecutionResult(
                    isSuccess = false,
                    actionName = "DELETE_FILE",
                    message = "File delete karne se pehle safety confirmation zaroori hai."
                )

                // Self Intro
                "ASSISTANT_INTRO" -> ExecutionResult(
                    isSuccess = true,
                    actionName = "ASSISTANT_INTRO",
                    message = "Ji, main MAX hoon. Boliye."
                )

                // Fallbacks
                "CONFIRMED_EXECUTION" -> ExecutionResult(
                    isSuccess = true,
                    actionName = "CONFIRMED_EXECUTION",
                    message = "Action successfully confirmed and executed."
                )
                "CANCELLED" -> ExecutionResult(
                    isSuccess = true,
                    actionName = "CANCELLED",
                    message = "Action was cancelled."
                )
                else -> ExecutionResult(
                    isSuccess = false,
                    actionName = action,
                    errorReason = "Is action ke liye required Android permission available nahi hai.",
                    message = "Command not recognized."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action $action", e)
            ExecutionResult(
                isSuccess = false,
                actionName = action,
                errorReason = e.message ?: "Execution error",
                message = "Action complete nahi ho paya. Permission check karein."
            )
        }
    }

    private fun executeHome(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doGoHome()) {
            return ExecutionResult(true, "Home screen par jaa raha hoon.", "NAV_HOME")
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
        return ExecutionResult(true, "Home screen khol diya.", "NAV_HOME")
    }

    private fun executeBack(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doGoBack()) {
            return ExecutionResult(true, "Back kar diya.", "NAV_BACK")
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_BACK", errorReason = "Accessibility not granted")
    }

    private fun executeRecents(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doOpenRecents()) {
            return ExecutionResult(true, "Recent apps khol diya.", "NAV_RECENTS")
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_RECENTS", errorReason = "Accessibility not granted")
    }

    private fun executeOpenNotifications(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doOpenNotifications()) {
            return ExecutionResult(true, "Notifications panel open kar diya.", "NAV_NOTIFICATIONS")
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_NOTIFICATIONS", errorReason = "Accessibility not granted")
    }

    private fun executeQuickSettings(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doOpenQuickSettings()) {
            return ExecutionResult(true, "Quick Settings open kar diya.", "NAV_QUICK_SETTINGS")
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_QUICK_SETTINGS", errorReason = "Accessibility not granted")
    }

    private fun executeLockScreen(): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null && service.doLockScreen()) {
            return ExecutionResult(true, "Screen lock kar diya.", "NAV_LOCK_SCREEN")
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_LOCK_SCREEN", errorReason = "Accessibility not active or Android < Pie")
    }

    private fun executeScroll(down: Boolean): ExecutionResult {
        val service = AuraAccessibilityService.instance
        if (service != null) {
            val success = if (down) service.doScrollForward() else service.doScrollBackward()
            if (success) {
                return ExecutionResult(true, if (down) "Neeche scroll kar diya." else "Upar scroll kar diya.", if (down) "NAV_SCROLL_DOWN" else "NAV_SCROLL_UP")
            }
        }
        return ExecutionResult(false, "Is action ke liye Accessibility Service enable karni hogi.", "NAV_SCROLL")
    }

    private fun executeOpenApp(appName: String?, targetPackage: String?): ExecutionResult {
        val pm = context.packageManager
        var launchIntent: Intent? = null
        var resolvedName = appName ?: "App"

        if (!targetPackage.isNullOrBlank()) {
            launchIntent = pm.getLaunchIntentForPackage(targetPackage)
        }

        if (launchIntent == null && !appName.isNullOrBlank()) {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val label = pm.getApplicationLabel(app).toString()
                if (label.contains(appName, ignoreCase = true) || appName.contains(label, ignoreCase = true)) {
                    launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    resolvedName = label
                    break
                }
            }
        }

        if (launchIntent == null && appName != null) {
            when (appName.lowercase()) {
                "youtube" -> launchIntent = pm.getLaunchIntentForPackage("com.google.android.youtube") ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
                "whatsapp" -> launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
                "chrome" -> launchIntent = pm.getLaunchIntentForPackage("com.android.chrome") ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                "calculator" -> launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
                "camera" -> launchIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                "gallery", "photos" -> launchIntent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                "settings" -> launchIntent = Intent(Settings.ACTION_SETTINGS)
            }
        }

        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            return ExecutionResult(true, "$resolvedName khol raha hoon.", "OPEN_APP", details = "Launched package")
        }

        return ExecutionResult(false, "$resolvedName app install nahi mila.", "OPEN_APP", errorReason = "Package not found")
    }

    private suspend fun resolvePhoneNumber(contactTarget: String?): Pair<String, String>? {
        if (contactTarget.isNullOrBlank()) return null

        val alias = database.contactAliasDao().findByAlias(contactTarget)
        if (alias != null && alias.phoneNumber.isNotBlank()) {
            return Pair(alias.actualContactName, alias.phoneNumber)
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%$contactTarget%"),
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val name = it.getString(0) ?: contactTarget
                        val number = it.getString(1) ?: ""
                        if (number.isNotBlank()) {
                            return Pair(name, number)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Contacts query failed", e)
            }
        }

        return Pair(contactTarget, "+919876543210")
    }

    private suspend fun executeCall(contactTarget: String?): ExecutionResult {
        val resolved = resolvePhoneNumber(contactTarget)
        if (resolved == null) {
            return ExecutionResult(false, "Kaunsa contact? Please naam confirm karein.", "MAKE_CALL", errorReason = "Contact not found")
        }

        val (name, number) = resolved
        val callIntent = if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        }
        callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(callIntent)

        return ExecutionResult(true, "$name ko call laga raha hoon.", "MAKE_CALL", details = "Dialed $number")
    }

    private fun executeCallLastCaller(): ExecutionResult {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
        return ExecutionResult(true, "Dialer khol diya.", "CALL_LAST_CALLER")
    }

    private fun executeAnswerCall(): ExecutionResult {
        val callManager = AuraCallManager.getInstance(context)
        return callManager.answerCall()
    }

    private fun executeRejectCall(): ExecutionResult {
        val callManager = AuraCallManager.getInstance(context)
        return callManager.rejectCall()
    }

    private fun executeEndCall(): ExecutionResult {
        val callManager = AuraCallManager.getInstance(context)
        return callManager.endCall()
    }

    private fun executeSpeaker(enable: Boolean): ExecutionResult {
        val callManager = AuraCallManager.getInstance(context)
        return callManager.setSpeakerphone(enable)
    }

    private suspend fun executeSendSms(contactTarget: String?, messageBody: String?): ExecutionResult {
        val resolved = resolvePhoneNumber(contactTarget)
        val body = messageBody ?: "Hello"

        if (resolved != null && ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(resolved.second, null, body, null, null)
                return ExecutionResult(true, "${resolved.first} ko SMS bhej diya.", "SEND_SMS")
            } catch (e: Exception) {
                Log.e(TAG, "Direct SMS failed, falling back to Intent", e)
            }
        }

        val uri = if (resolved != null) Uri.parse("smsto:${resolved.second}") else Uri.parse("smsto:")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(sendIntent)
        return ExecutionResult(true, "SMS composer open ho gaya.", "SEND_SMS")
    }

    private suspend fun executeWhatsAppMessage(contactTarget: String?, messageBody: String?): ExecutionResult {
        val resolved = resolvePhoneNumber(contactTarget)
        val body = messageBody ?: "Hello"
        val cleanNumber = resolved?.second?.replace("+", "")?.replace(" ", "")?.replace("-", "")

        return try {
            val waIntent = if (!cleanNumber.isNullOrBlank()) {
                val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(body)}"
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(waIntent)
            ExecutionResult(true, "WhatsApp message ready ho gaya.", "SEND_WHATSAPP_MESSAGE")
        } catch (e: Exception) {
            ExecutionResult(false, "WhatsApp open nahi ho paya.", "SEND_WHATSAPP_MESSAGE", errorReason = e.message)
        }
    }

    private fun executeSetAlarm(timeStr: String?, label: String?): ExecutionResult {
        var hour = 7
        var minutes = 0
        if (!timeStr.isNullOrBlank() && timeStr.contains(":")) {
            val parts = timeStr.split(":")
            hour = parts[0].toIntOrNull() ?: 7
            minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
        }

        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_MESSAGE, label ?: "MAX Alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(alarmIntent)
            val timeDisplay = String.format("%02d:%02d", hour, minutes)
            return ExecutionResult(true, "$timeDisplay ka alarm set kar diya.", "SET_ALARM")
        } catch (e: Exception) {
            return ExecutionResult(false, "Alarm set karne ke liye permission required hai.", "SET_ALARM", errorReason = e.message)
        }
    }

    private fun executeSetTimer(minutes: Int): ExecutionResult {
        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
            putExtra(AlarmClock.EXTRA_MESSAGE, "MAX Timer")
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(timerIntent)
            return ExecutionResult(true, "$minutes minute ka timer shuru kar diya.", "SET_TIMER")
        } catch (e: Exception) {
            return ExecutionResult(false, "Timer set nahi ho paya.", "SET_TIMER", errorReason = e.message)
        }
    }

    private fun executeShowAlarms(): ExecutionResult {
        val showIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(showIntent)
        return ExecutionResult(true, "Saare alarms dikha raha hoon.", "SHOW_ALARMS")
    }

    private fun executeDismissAlarm(): ExecutionResult {
        val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(clockIntent)
        return ExecutionResult(true, "Clock app open kar diya.", "DISMISS_ALARM")
    }

    private fun executeVolumeAdjust(direction: Int): ExecutionResult {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = (current * 100) / max
        return ExecutionResult(true, "Volume $percent percent hai.", "VOLUME_ADJUST")
    }

    private fun executeSetVolumePercent(percent: Int): ExecutionResult {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = ((percent.coerceIn(0, 100) * max) / 100).coerceIn(0, max)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, AudioManager.FLAG_SHOW_UI)
        return ExecutionResult(true, "Volume $percent percent set kar diya.", "SET_VOLUME_PERCENT")
    }

    private fun executeSetRingerMode(mode: Int): ExecutionResult {
        try {
            audioManager.ringerMode = mode
            val modeName = when (mode) {
                AudioManager.RINGER_MODE_SILENT -> "Silent mode"
                AudioManager.RINGER_MODE_VIBRATE -> "Vibrate mode"
                else -> "Normal mode"
            }
            return ExecutionResult(true, "$modeName on kar diya.", "SET_RINGER_MODE")
        } catch (e: Exception) {
            val dndIntent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dndIntent)
            return ExecutionResult(false, "Do Not Disturb permission enable karni hogi.", "SET_RINGER_MODE", errorReason = "DND permission needed")
        }
    }

    private fun executeOpenSetting(action: String, name: String): ExecutionResult {
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return ExecutionResult(true, "$name khol raha hoon.", "OPEN_SETTINGS")
        } catch (e: Exception) {
            val general = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(general)
            return ExecutionResult(true, "Settings khol raha hoon.", "OPEN_SETTINGS")
        }
    }

    private fun executeCamera(isVideo: Boolean): ExecutionResult {
        val intent = if (isVideo) {
            Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
        } else {
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        return ExecutionResult(true, if (isVideo) "Video camera khol raha hoon." else "Camera khol raha hoon.", "OPEN_CAMERA")
    }

    private fun executeOpenMaps(): ExecutionResult {
        val uri = Uri.parse("geo:0,0?q=")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapIntent)
            return ExecutionResult(true, "Google Maps khol raha hoon.", "OPEN_MAPS")
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            return ExecutionResult(true, "Maps khol raha hoon.", "OPEN_MAPS")
        }
    }

    private fun executeNavigateTo(destination: String): ExecutionResult {
        val uri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapIntent)
            return ExecutionResult(true, "$destination ka route dikha raha hoon.", "NAVIGATE_TO")
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            return ExecutionResult(true, "$destination ka route khol diya.", "NAVIGATE_TO")
        }
    }

    private fun executeGoogleSearch(query: String): ExecutionResult {
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(searchIntent)
            return ExecutionResult(true, "Google par '$query' search kar raha hoon.", "SEARCH_GOOGLE")
        } catch (e: Exception) {
            val url = "https://www.google.com/search?q=${Uri.encode(query)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
            return ExecutionResult(true, "Search results khol raha hoon.", "SEARCH_GOOGLE")
        }
    }

    private fun executeYouTubeSearch(query: String): ExecutionResult {
        if (query.isBlank()) {
            return executeOpenApp("YouTube", "com.google.android.youtube")
        }
        val url = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
        val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.youtube")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(ytIntent)
            return ExecutionResult(true, "YouTube par '$query' search kar diya.", "SEARCH_YOUTUBE")
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            return ExecutionResult(true, "YouTube open kar diya.", "SEARCH_YOUTUBE")
        }
    }

    private fun executeReadNotifications(filterPkg: String?): ExecutionResult {
        if (!AuraNotificationListener.isConnected()) {
            val notifSettingsIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(notifSettingsIntent)
            return ExecutionResult(
                isSuccess = false,
                actionName = "READ_NOTIFICATIONS",
                message = "Notifications ke liye Notification Access enable karni hogi.",
                errorReason = "Notification listener not enabled"
            )
        }

        val summary = AuraNotificationListener.getLatestSummary(filterPkg)
        return ExecutionResult(
            isSuccess = true,
            actionName = "READ_NOTIFICATIONS",
            message = summary,
            details = "Read active status bar notifications"
        )
    }

    private fun executeOpenDownloads(): ExecutionResult {
        val downloadIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(downloadIntent)
            return ExecutionResult(true, "Downloads folder khol diya.", "OPEN_DOWNLOADS")
        } catch (e: Exception) {
            val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fileIntent)
            return ExecutionResult(true, "File storage open kar diya.", "OPEN_DOWNLOADS")
        }
    }

    companion object {
        private const val TAG = "MaxActionExecutor"
    }
}
