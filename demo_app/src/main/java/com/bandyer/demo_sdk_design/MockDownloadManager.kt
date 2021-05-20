package com.bandyer.demo_sdk_design

import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.communication_center.file_share.file_sharing_center.utils.Downloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.HashMap

class MockDownloadManager: Downloader {
    override val downloads: StateFlow<HashMap<String, DownloadState>>
        get() = TODO("Not yet implemented")
    override val scope: CoroutineScope
        get() = TODO("Not yet implemented")
    override val events: SharedFlow<DownloadState>
        get() = TODO("Not yet implemented")

    override fun download(downloadId: String, endpoint: String, file: File): String {
        TODO("Not yet implemented")
    }

    override fun cancel(downloadId: String) {
        TODO("Not yet implemented")
    }
}