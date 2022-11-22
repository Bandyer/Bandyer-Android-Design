package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel

internal class CallActionsViewModel : BaseViewModel<CallActionsUiState>() {
    override fun initialState() = CallActionsUiState()
}