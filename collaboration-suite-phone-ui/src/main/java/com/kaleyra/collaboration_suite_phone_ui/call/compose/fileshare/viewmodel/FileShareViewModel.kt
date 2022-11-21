package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import com.kaleyra.collaboration_suite_phone_ui.call.compose.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState

internal class FileShareViewModel : BaseViewModel<FileShareUiState>() {
    override fun initialState() = FileShareUiState()
}



