package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    fun enableMicrophone(enable: Boolean) {

    }

    fun enableCamera(enable: Boolean) {

    }

    fun switchCamera() {

    }

    fun hangUp() {

    }
}