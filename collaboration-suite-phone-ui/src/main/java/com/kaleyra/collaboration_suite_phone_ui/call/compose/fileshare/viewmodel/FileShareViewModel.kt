package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class FileShareViewModel : BaseViewModel<FileShareState>() {
    override fun initialState() = FileShareState(transferList = ImmutableList(listOf()), userMessage = null)
}



