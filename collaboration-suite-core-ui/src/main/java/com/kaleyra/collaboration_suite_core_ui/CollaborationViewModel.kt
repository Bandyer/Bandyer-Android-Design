package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class CollaborationViewModel: ViewModel() {

    private val _isCollaborationConfigured = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            _isCollaborationConfigured.emit(requestConfigure())
        }
    }

    val isCollaborationConfigured = _isCollaborationConfigured.asSharedFlow()

    /**
     * Checking if the CollaborationUI is configured. If it is not, it is requesting a new configuration.
     * @return true if is configured, false otherwise
     **/
    private suspend fun requestConfigure(): Boolean {
        if (!CollaborationUI.isConfigured) CollaborationService.get()?.onRequestNewCollaborationConfigure()
        return CollaborationUI.isConfigured
    }
}