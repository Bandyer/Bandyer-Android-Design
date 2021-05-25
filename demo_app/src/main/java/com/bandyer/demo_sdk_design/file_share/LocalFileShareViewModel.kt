package com.bandyer.demo_sdk_design.file_share

import com.bandyer.sdk_design.filesharing.FileShareItemData
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class LocalFileShareViewModel: FileShareViewModel() {

    override var itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    private val uploader =  LocalUploadManager.newInstance()

    private val downloader = LocalDownloadManager.newInstance()

    override fun upload(uploadId: String?, file: File, keepFileOnSuccess: Boolean) =
        if(uploadId == null) uploader.upload(file = file, keepFileOnSuccess = keepFileOnSuccess)
        else uploader.upload(uploadId, file, keepFileOnSuccess)

    override fun cancelUpload(uploadId: String) = uploader.cancel(uploadId)

    override fun download(downloadId: String?, endpoint: String, file: File) =
        if(downloadId == null) downloader.download(endpoint = endpoint, file = file)
        else downloader.download(downloadId, endpoint, file)

    override fun cancelDownload(downloadId: String) = downloader.cancel(downloadId)
}