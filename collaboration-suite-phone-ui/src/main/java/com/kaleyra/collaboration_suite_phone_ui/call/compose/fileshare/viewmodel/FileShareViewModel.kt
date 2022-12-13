package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel

import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.viewmodel.BaseViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

internal class FileShareViewModel : BaseViewModel<FileShareUiState>() {
//    override fun initialState() = FileShareUiState()
    override fun initialState() = FileShareUiState(
        transferList = ImmutableList(
            listOf(
                mockDownloadTransfer.copy(
                    id = "0",
                    state = TransferUi.State.Success(
                        Uri.EMPTY
                    )
                ),
                mockUploadTransfer.copy(id = "1"),
                mockUploadTransfer.copy(id = "2"),
                mockUploadTransfer.copy(id = "3"),
                mockUploadTransfer.copy(id = "4"),
                mockUploadTransfer.copy(id = "5"),
                mockUploadTransfer.copy(id = "6"),
                mockUploadTransfer.copy(id = "7"),
                mockUploadTransfer.copy(id = "8"),
                mockUploadTransfer.copy(id = "9"),
                mockUploadTransfer.copy(id = "10"),
                mockUploadTransfer.copy(id = "11"),
                mockUploadTransfer.copy(id = "12"),
                mockUploadTransfer.copy(id = "13"),
                mockUploadTransfer.copy(id = "14"),
                mockUploadTransfer.copy(id = "15"),
                mockUploadTransfer.copy(id = "16"),
                mockUploadTransfer.copy(id = "17"),
                mockUploadTransfer.copy(id = "18"),
                mockUploadTransfer.copy(id = "19"),
                mockUploadTransfer.copy(id = "20"),
                mockUploadTransfer.copy(id = "21"),
                mockUploadTransfer.copy(id = "22"),
                mockUploadTransfer.copy(id = "23"),
                mockUploadTransfer.copy(id = "24"),
                mockUploadTransfer.copy(id = "25"),
                mockUploadTransfer.copy(id = "26")
            )
        )
    )
}



