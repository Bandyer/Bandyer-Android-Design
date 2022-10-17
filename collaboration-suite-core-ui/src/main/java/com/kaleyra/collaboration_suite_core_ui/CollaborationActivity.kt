package com.kaleyra.collaboration_suite_core_ui

import androidx.appcompat.app.AppCompatActivity

/**
 * Collaboration activity
 */
abstract class CollaborationActivity : AppCompatActivity() {

    /**
     * Checking if the CollaborationUI is configured. If it is not, it is requesting a new configuration.
     * @return Configuration.Success if it is configured, Configuration.Failure otherwise
     **/
    protected suspend fun requestConfigure(): Boolean {
        if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
        return CollaborationUI.isConfigured
    }
}