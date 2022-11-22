package com.kaleyra.collaboration_suite_phone_ui.call.compose.callaction.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class CallActionsUiState(
    val actionList: ImmutableList<CallAction> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState