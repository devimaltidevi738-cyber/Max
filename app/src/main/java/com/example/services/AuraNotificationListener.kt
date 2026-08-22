package com.example.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuraNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isListenerActive.value = true
        refreshNotifications()
        Log.d(TAG, "AuraNotificationListener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
            _isListenerActive.value = false
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshNotifications()
    }

    fun refreshNotifications() {
        try {
            val active = activeNotifications ?: return
            val items = active.mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                if (title.isBlank() && text.isBlank()) null
                else {
                    val appName = try {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(sbn.packageName, 0)
                        ).toString()
                    } catch (e: Exception) {
                        sbn.packageName
                    }
                    NotificationItem(
                        id = sbn.key,
                        packageName = sbn.packageName,
                        appName = appName,
                        title = title,
                        text = text,
                        timestamp = sbn.postTime
                    )
                }
            }
            _currentNotifications.value = items
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active notifications", e)
        }
    }

    companion object {
        private const val TAG = "AuraNotifListener"
        var instance: AuraNotificationListener? = null
            private set

        private val _isListenerActive = MutableStateFlow(false)
        val isListenerActive = _isListenerActive.asStateFlow()

        private val _currentNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val currentNotifications = _currentNotifications.asStateFlow()

        fun isConnected(): Boolean = instance != null

        fun getLatestSummary(filterPackage: String? = null): String {
            val list = _currentNotifications.value
            val filtered = if (filterPackage != null) {
                list.filter { it.packageName.contains(filterPackage, ignoreCase = true) || it.appName.contains(filterPackage, ignoreCase = true) }
            } else {
                list
            }

            if (filtered.isEmpty()) {
                return if (filterPackage != null) {
                    "$filterPackage ka koi naya notification nahi hai."
                } else {
                    "Abhi koi naye notifications nahi hain."
                }
            }

            val sb = StringBuilder()
            sb.append("Aapke paas ${filtered.size} notifications hain. ")
            filtered.take(3).forEach { item ->
                sb.append("${item.appName} se: ${item.title} - ${item.text}. ")
            }
            return sb.toString()
        }
    }
}
