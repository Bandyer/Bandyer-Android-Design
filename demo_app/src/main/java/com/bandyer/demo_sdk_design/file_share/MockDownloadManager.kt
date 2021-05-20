package com.bandyer.demo_sdk_design.file_share

import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpDownloader
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpStack
import com.bandyer.communication_center.file_share.file_sharing_center.request.invokeOnCancelled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MockDownloadManager(override val httpStack: HttpStack, override val scope: CoroutineScope): HttpDownloader {

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val downloads: StateFlow<HashMap<String, DownloadState>> = MutableStateFlow(hashMapOf())

    override val events = MutableSharedFlow<DownloadState>()

    override fun download(downloadId: String, endpoint: String, file: File): String {
        val startTime = Date().time
        val totalBytes = 100L
        val nOfUpdates = 10

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(DownloadState.Pending(downloadId,"", file, startTime, totalBytes))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(DownloadState.OnProgress(downloadId,"", file, startTime, totalBytes,(totalBytes / nOfUpdates) * i))
            }
            events.emit(DownloadState.Success(downloadId,"", file, startTime, totalBytes))
        }.apply {
            jobs[downloadId] = this
            invokeOnCancelled(scope.coroutineContext) {
                events.emit(DownloadState.Cancelled(downloadId,"", file, startTime, totalBytes))
            }
            start()
        }

        return downloadId
    }

    override fun cancel(downloadId: String) { jobs[downloadId]?.cancel() }
}