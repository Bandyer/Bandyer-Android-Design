package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

val mockUploadTransfer = TransferUi(
    id = "1",
    file = FileUi(
        name = "upload.txt",
        type = FileUi.Type.Media,
        size = 23333L
    ),
    sender = "Mario",
    time = 324234L,
    state = TransferUi.State.InProgress(progress = .7f),
    type = TransferUi.Type.Upload
)

val mockDownloadTransfer = TransferUi(
    id = "2",
    file = FileUi(
        name = "download.txt",
        type = FileUi.Type.Miscellaneous,
        size = 40000L
    ),
    sender = "Keanu",
    time = 3254234L,
    state = TransferUi.State.InProgress(progress = .4f),
    type = TransferUi.Type.Download
)