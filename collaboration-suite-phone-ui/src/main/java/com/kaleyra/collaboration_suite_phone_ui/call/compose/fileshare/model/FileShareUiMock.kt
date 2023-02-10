package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

val mockUploadSharedFile = SharedFileUi(
    id = "1",
    file = FileUi(
        name = "upload.txt",
        type = FileUi.Type.Media,
        size = 23333L
    ),
    sender = "Mario",
    time = 324234L,
    state = SharedFileUi.State.InProgress(progress = .7f),
    type = SharedFileUi.Type.Upload
)

val mockDownloaSharedFile = SharedFileUi(
    id = "2",
    file = FileUi(
        name = "download.txt",
        type = FileUi.Type.Miscellaneous,
        size = 40000L
    ),
    sender = "Keanu",
    time = 3254234L,
    state = SharedFileUi.State.InProgress(progress = .4f),
    type = SharedFileUi.Type.Download
)