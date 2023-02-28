package com.kaleyra.collaboration_suite_core_ui.vppa

import android.content.BroadcastReceiver

abstract class VPPABroadcastReceiver: BroadcastReceiver() {

    companion object {
        const val ACTION_ACCEPT_TERM = "com.kaleyra.collaboration_suite_core_ui.vppa.ACTION_ACCEPT_TERM"
        const val ACTION_DECLINE_TERM = "com.kaleyra.collaboration_suite_core_ui.vppa.ACTION_DECLINE_TERM"
        const val ACTION_CANCEL = "com.kaleyra.collaboration_suite_core_ui.vppa.ACTION_CANCEL"
        const val EXTRA_ID = "com.kaleyra.collaboration_suite_core_ui.vppa.EXTRA_ID"
    }
}