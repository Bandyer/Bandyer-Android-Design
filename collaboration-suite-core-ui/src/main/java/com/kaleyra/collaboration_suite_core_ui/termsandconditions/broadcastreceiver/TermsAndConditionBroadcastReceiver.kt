package com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class TermsAndConditionBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ACCEPT = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_DECLINE"
        const val ACTION_CANCEL = "com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.ACTION_CANCEL"
    }

    fun registerForTermAndConditionAction(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_ACCEPT)
            addAction(ACTION_DECLINE)
            addAction(ACTION_DECLINE)
        })
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