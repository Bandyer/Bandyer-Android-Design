package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState

internal class FileShareViewModel(configure: suspend () -> Configuration) : BaseViewModel<FileShareUiState>(configure) {
    override fun initialState() = FileShareUiState()
}



