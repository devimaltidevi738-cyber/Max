package com.example.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuraAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        Log.d(TAG, "AuraAccessibilityService Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Track active window or package changes
        event?.packageName?.let {
            _currentForegroundPackage.value = it.toString()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "AuraAccessibilityService Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceActive.value = false
        }
    }

    fun doGoHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun doGoBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun doOpenRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun doOpenNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun doOpenQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    fun doLockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    fun doScrollForward(): Boolean {
        val root = rootInActiveWindow ?: return false
        return scrollNode(root, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    fun doScrollBackward(): Boolean {
        val root = rootInActiveWindow ?: return false
        return scrollNode(root, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    private fun scrollNode(node: AccessibilityNodeInfo, action: Int): Boolean {
        if (node.isScrollable && (node.actions and action != 0)) {
            return node.performAction(action)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scrollNode(child, action)) {
                return true
            }
        }
        return false
    }

    fun findAndClickByText(query: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val matchingNodes = root.findAccessibilityNodeInfosByText(query)
        if (matchingNodes != null && matchingNodes.isNotEmpty()) {
            for (node in matchingNodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isClickable) {
                        return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    current = current.parent
                }
            }
        }
        return false
    }

    fun getVisibleScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val stringBuilder = StringBuilder()
        collectText(root, stringBuilder)
        return stringBuilder.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo, builder: StringBuilder) {
        if (!node.text.isNullOrBlank()) {
            builder.append(node.text).append("\n")
        } else if (!node.contentDescription.isNullOrBlank()) {
            builder.append(node.contentDescription).append("\n")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, builder)
        }
    }

    companion object {
        private const val TAG = "AuraAccessibility"
        var instance: AuraAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        private val _currentForegroundPackage = MutableStateFlow("")
        val currentForegroundPackage = _currentForegroundPackage.asStateFlow()

        fun isConnected(): Boolean = instance != null
    }
}
