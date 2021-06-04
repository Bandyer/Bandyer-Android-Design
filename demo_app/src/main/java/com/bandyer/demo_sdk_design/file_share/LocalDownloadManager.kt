package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.bandyer.communication_center.file_share.file_sharing_center.DefaultFileSharingConfig
import com.bandyer.communication_center.file_share.file_sharing_center.FileSharingConfig
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpDownloader
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpStack
import com.bandyer.communication_center.file_share.file_sharing_center.request.invokeOnCancelled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.OutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LocalDownloadManager private constructor(override val httpStack: HttpStack, override val scope: CoroutineScope): HttpDownloader {

    companion object {
        fun newInstance(config: FileSharingConfig = DefaultFileSharingConfig): HttpDownloader = LocalDownloadManager(config.httpStack, config.ioScope)
    }

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val downloads: StateFlow<HashMap<String, DownloadState>> = MutableStateFlow(hashMapOf())

    override val events = MutableSharedFlow<DownloadState>()

    override fun download(downloadId: String, endpoint: String, context: Context): String {
        val startTime = Date().time
        val totalBytes = 100L
        val nOfUpdates = 10
        val uri = "".toUri()

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(DownloadState.Pending(downloadId,"", uri, startTime, totalBytes))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(DownloadState.OnProgress(downloadId,"", uri, startTime, totalBytes,(totalBytes / nOfUpdates) * i))
            }
            events.emit(DownloadState.Success(downloadId,"", uri, startTime, totalBytes))
        }.apply {
            jobs[downloadId] = this
            invokeOnCancelled(scope.coroutineContext) {
                events.emit(DownloadState.Cancelled(downloadId,"", uri, startTime, totalBytes))
            }
            start()
        }

        return downloadId
    }

    override fun cancel(downloadId: String) { jobs[downloadId]?.cancel() }
}