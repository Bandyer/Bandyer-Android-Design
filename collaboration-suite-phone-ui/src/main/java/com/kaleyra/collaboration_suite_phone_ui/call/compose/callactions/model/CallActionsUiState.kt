package com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model

import androidx.compose.runtime.Immutable
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@Immutable
internal data class CallActionsUiState(
    val actionList: ImmutableList<CallAction> = ImmutableList(emptyList())
) : UiState