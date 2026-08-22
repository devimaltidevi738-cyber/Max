package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.d(TAG, "onReceive action: $action")

        if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
            Log.d(TAG, "Phone state changed: $state, number: $incomingNumber")

            val callManager = AuraCallManager.getInstance(context)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                // If caller number is available, update state
                // AuraCallManager handles this
            }
        }
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}
