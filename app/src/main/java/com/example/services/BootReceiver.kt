package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "BootReceiver received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == "android.intent.action.QUICKBOOT_POWERON" || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // Check if user has granted microphone permission
            val micGranted = ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            val prefs = context.getSharedPreferences("max_settings", Context.MODE_PRIVATE)
            val autoStartEnabled = prefs.getBoolean("pref_auto_start_background", true)

            if (autoStartEnabled && micGranted) {
                Log.d(TAG, "Restoring MAX Foreground Background Service after reboot")
                try {
                    AdiForegroundService.start(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore MAX Foreground Service on boot", e)
                }
            } else {
                Log.d(TAG, "Skipping background service restore (autoStart=$autoStartEnabled, micGranted=$micGranted)")
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
