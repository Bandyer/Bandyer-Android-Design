package com.kaleyra.collaboration_suite_phone_ui.call.component.callactions.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.common.uistate.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class CallActionsUiState(
    val actionList: ImmutableList<CallAction> = ImmutableList(emptyList())
) : UiState