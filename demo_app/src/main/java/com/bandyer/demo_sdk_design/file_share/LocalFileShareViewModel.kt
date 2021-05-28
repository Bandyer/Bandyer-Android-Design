package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import android.net.Uri
import com.bandyer.sdk_design.filesharing.FileShareViewModel

class LocalFileShareViewModel: FileShareViewModel() {

    private val uploader =  LocalUploadManager.newInstance()

    private val downloader = LocalDownloadManager.newInstance()

    val uploadEvents = uploader.events

    val downloadEvents = downloader.events

    override fun upload(uploadId: String?, context: Context, uri: Uri): String =
        if(uploadId == null) uploader.upload(context = context, uri = uri)
        else  uploader.upload(uploadId, context = context, uri = uri)

    override fun cancelUpload(uploadId: String) = uploader.cancel(uploadId)

    override fun download(downloadId: String?, endpoint: String, context: Context): String =
        if(downloadId == null) downloader.download(endpoint = endpoint, context = context)
        else downloader.download(downloadId, endpoint, context)


    override fun cancelDownload(downloadId: String) = downloader.cancel(downloadId)
}