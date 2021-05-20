package com.bandyer.demo_sdk_design

import com.bandyer.communication_center.file_share.file_sharing_center.FileSharingConfig
import com.bandyer.communication_center.file_share.file_sharing_center.networking.models.AmazonTransferPolicy
import com.bandyer.communication_center.file_share.file_sharing_center.request.AmazonUploader
import com.bandyer.communication_center.file_share.file_sharing_center.request.FileSharer
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpDownloader
import com.bandyer.communication_center.file_share.file_sharing_center.request.OkHttpStack
import kotlinx.coroutines.GlobalScope
import java.io.File

class MockFileSharingManager(override val uploadManager: AmazonUploader, override val downloadManager: HttpDownloader, override val config: FileSharingConfig): FileSharer {

    override fun fileUpload(file: File, uploadId: String) = upload(uploadId, file)

    override fun snapshotUpload(file: File, uploadId: String) = upload(uploadId, file)

    override fun whiteboardUpload(file: File, uploadId: String) = upload(uploadId, file)

    override fun cancelUpload(uploadId: String) = uploadManager.cancel(uploadId)

    override fun download(url: String, file: File, downloadId: String) = downloadManager.download(downloadId, url, file)

    override fun cancelDownload(downloadId: String) = downloadManager.cancel(downloadId)

    private fun upload(uploadId: String, file: File) = uploadManager.upload(uploadId, AmazonTransferPolicy("","","","","", hashMapOf()), file)

    companion object {
        fun newInstance(config: FileSharingConfig): FileSharer {
            val uploadManager = MockUploadManager(OkHttpStack(), GlobalScope)
            val downloadManager = MockDownloadManager(OkHttpStack(), GlobalScope)
            return MockFileSharingManager(uploadManager, downloadManager, config)
        }
    }
}