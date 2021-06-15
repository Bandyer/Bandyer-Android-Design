package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import androidx.core.net.toUri
import com.bandyer.communication_center.file_share.config.DefaultFileSharingConfig
import com.bandyer.communication_center.file_share.config.FileSharingConfig
import com.bandyer.communication_center.file_share.downloader.HttpDownloader
import com.bandyer.communication_center.file_share.events.DownloadEvent
import com.bandyer.communication_center.file_share.http_stack.HttpStack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LocalDownloadManager private constructor(override val httpStack: HttpStack, override val scope: CoroutineScope):
    HttpDownloader {

    companion object {
        fun newInstance(config: FileSharingConfig = DefaultFileSharingConfig): HttpDownloader = LocalDownloadManager(config.httpStack, config.ioScope)
    }

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val events = MutableSharedFlow<DownloadEvent>()

    override fun download(downloadId: String, endpoint: String, context: Context): String {
        val startTime = Date().time
        val totalBytes = 100L
        val nOfUpdates = 10
        val uri = "".toUri()

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(DownloadEvent.Pending(downloadId,"", uri, startTime, totalBytes))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(DownloadEvent.OnProgress(downloadId,"", uri, startTime, totalBytes,(totalBytes / nOfUpdates) * i))
            }
            events.emit(DownloadEvent.Error(downloadId,"", uri, startTime, totalBytes, Throwable()))
        }.apply {
            jobs[downloadId] = this
            invokeOnCompletion {
                if(it is CancellationException) scope.launch { events.emit(DownloadEvent.Cancelled(downloadId,"", uri, startTime, totalBytes)) }
            }
            start()
        }

        return downloadId
    }

    override fun cancel(downloadId: String) { jobs[downloadId]?.cancel() }
}