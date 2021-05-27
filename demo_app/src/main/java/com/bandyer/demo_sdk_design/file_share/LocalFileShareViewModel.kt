package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import android.net.Uri
import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.sdk_design.filesharing.FileShareViewModel
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

class LocalFileShareViewModel: FileShareViewModel() {

    private val uploader =  LocalUploadManager.newInstance()

    private val downloader = LocalDownloadManager.newInstance()

    val uploadEvents: SharedFlow<UploadState> = uploader.events

    val downloadEvents: SharedFlow<DownloadState> = downloader.events

    override fun upload(uploadId: String?, context: Context, uri: Uri) =
        if(uploadId == null) uploader.upload(context = context, uri = uri)
        else  uploader.upload(uploadId, context = context, uri = uri)

    override fun cancelUpload(uploadId: String) = uploader.cancel(uploadId)

    override fun download(downloadId: String?, endpoint: String, file: File) = ""
//        if(downloadId == null) downloader.download(endpoint = endpoint, file = file)
//        else downloader.download(downloadId, endpoint, file)

    override fun cancelDownload(downloadId: String) = downloader.cancel(downloadId)
}