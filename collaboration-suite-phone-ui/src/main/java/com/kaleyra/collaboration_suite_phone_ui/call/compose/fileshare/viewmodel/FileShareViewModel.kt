package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class FileShareViewModel : BaseViewModel<FileShareUiState>() {
    override fun initialState() = FileShareUiState(transferList = ImmutableList(listOf(mockDownloadTransfer.copy(state = TransferUi.State.Success(
        Uri.EMPTY)), mockUploadTransfer)) )
}



