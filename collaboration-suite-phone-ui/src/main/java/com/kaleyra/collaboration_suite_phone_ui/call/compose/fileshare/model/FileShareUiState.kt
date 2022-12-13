package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.model.UiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal data class FileShareUiState(
    val transferList: ImmutableList<TransferUi> = ImmutableList(emptyList()),
    override val userMessage: String? = null
) : UiState {

    fun transfersListUpdated(transferList: ImmutableList<TransferUi>) = copy(transferList = transferList)

    fun userMessageReceived(message: String) = copy(userMessage = message)

    fun userMessageShown() = copy(userMessage = null)
}
