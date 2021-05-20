package com.bandyer.demo_sdk_design

import com.bandyer.communication_center.file_share.file_sharing_center.FileSharingConfig
import com.bandyer.communication_center.file_share.file_sharing_center.UploadManager
import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadManager
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.FileSharer
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.HashMap

class MockFileSharingManager: FileSharer {

    override val uploadManager: UploadManager
        get() = TODO("Not yet implemented")

    override val downloadManager: DownloadManager
        get() = TODO("Not yet implemented")

    override val config: FileSharingConfig
        get() = TODO("Not yet implemented")

    override val uploads: StateFlow<HashMap<String, UploadState>>
        get() = TODO("Not yet implemented")

    override val downloads: StateFlow<HashMap<String, DownloadState>>
        get() = TODO("Not yet implemented")

    override fun fileUpload(file: File, uploadId: String): String {
        TODO("Not yet implemented")
    }

    override fun snapshotUpload(file: File, uploadId: String): String {
        TODO("Not yet implemented")
    }

    override fun whiteboardUpload(file: File, uploadId: String): String {
        TODO("Not yet implemented")
    }

    override fun cancelUpload(uploadId: String) {
        TODO("Not yet implemented")
    }

    override fun download(url: String, file: File, downloadId: String): String {
        TODO("Not yet implemented")
    }

    override fun cancelDownload(downloadId: String) {
        TODO("Not yet implemented")
    }
}