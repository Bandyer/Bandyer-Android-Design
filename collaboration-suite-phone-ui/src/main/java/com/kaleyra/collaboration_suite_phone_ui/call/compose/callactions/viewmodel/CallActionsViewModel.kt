package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel

internal class CallActionsViewModel : BaseViewModel<CallActionsUiState>() {
    override fun initialState() = CallActionsUiState(actionList = mockCallActions)
}