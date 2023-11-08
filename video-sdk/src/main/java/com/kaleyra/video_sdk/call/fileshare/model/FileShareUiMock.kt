package com.kaleyra.video_sdk.call.fileshare.model

import android.net.Uri
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

val mockUploadSharedFile = SharedFileUi(
    id = "1",
    name = "upload.txt",
    uri = ImmutableUri(Uri.EMPTY),
    size = 23333L,
    sender = "Mario",
    time = 324234L,
    state = SharedFileUi.State.InProgress(progress = .7f),
    isMine = true
)

val mockDownloadSharedFile = SharedFileUi(
    id = "2",
    name = "download.txt",
    uri = ImmutableUri(Uri.EMPTY),
    size = 40000L,
    sender = "Keanu",
    time = 3254234L,
    state = SharedFileUi.State.InProgress(progress = .4f),
    isMine = false
)