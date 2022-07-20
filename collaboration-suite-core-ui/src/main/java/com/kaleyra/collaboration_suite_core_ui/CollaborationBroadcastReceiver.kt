package com.kaleyra.collaboration_suite_core_ui

import android.content.BroadcastReceiver

/**
 * Collaboration broadcast receiver
 */
abstract class CollaborationBroadcastReceiver : BroadcastReceiver() {

    /**
     * Checking if the CollaborationUI is configured. If it is not, it is requesting a new configuration.
     * @return true if is configured, false otherwise
     **/
    protected suspend fun requestConfigure(): Boolean {
        if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
        return CollaborationUI.isConfigured
    }
}