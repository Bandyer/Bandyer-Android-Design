package com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

abstract class TermsAndConditionBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ACCEPT = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_DECLINE"
        const val ACTION_CANCEL = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_CANCEL"
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun registerForTermAndConditionAction(context: Context) {
        val filter = IntentFilter().apply {
            addAction(ACTION_ACCEPT)
            addAction(ACTION_DECLINE)
            addAction(ACTION_DECLINE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context.registerReceiver(this, filter, RECEIVER_NOT_EXPORTED)
        else context.registerReceiver(this, filter)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ACCEPT && intent.action != ACTION_DECLINE && intent.action != ACTION_CANCEL) return
        when (intent.action) {
            ACTION_ACCEPT -> onActionAccept()
            ACTION_DECLINE -> onActionDecline()
            ACTION_CANCEL -> onActionCancel()
        }
        context.unregisterReceiver(this)
    }

    abstract fun onActionAccept()

    abstract fun onActionDecline()

    abstract fun onActionCancel()

}