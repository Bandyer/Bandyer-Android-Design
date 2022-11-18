package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class FileShareState(
    val transfersList: ImmutableList<TransferUi> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState {

    fun transfersListUpdated(transfersList: ImmutableList<TransferUi>) = copy(transfersList = transfersList)

    fun userMessageReceived(message: String) = copy(userMessage = message)

    fun userMessageShown() = copy(userMessage = null)
}
