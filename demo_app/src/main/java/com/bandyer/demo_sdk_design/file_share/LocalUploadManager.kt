package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import android.net.Uri
import com.bandyer.communication_center.file_share.config.DefaultFileSharingConfig
import com.bandyer.communication_center.file_share.config.FileSharingConfig
import com.bandyer.communication_center.file_share.events.UploadEvent
import com.bandyer.communication_center.file_share.http_stack.HttpStack
import com.bandyer.communication_center.file_share.uploader.HttpUploader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LocalUploadManager private constructor(override val httpStack: HttpStack, override val scope: CoroutineScope):
    HttpUploader {

    companion object {
        fun newInstance(config: FileSharingConfig = DefaultFileSharingConfig): HttpUploader = LocalUploadManager(config.httpStack, config.ioScope)
    }

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val events = MutableSharedFlow<UploadEvent>()

    override fun upload(uploadId: String, context: Context, uri: Uri): String {
        val startTime = Date().time
        val nOfUpdates = 10
        val file = try { FileUtils.from(context, uri) } catch (ex: Exception) { File("") }
        val totalBytes = if(file.length() != 0L) file.length() else 100L

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(UploadEvent.Pending(uploadId, startTime, totalBytes, uri))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(UploadEvent.OnProgress(uploadId, startTime, totalBytes, uri,(totalBytes / nOfUpdates) * i))
            }
            events.emit(UploadEvent.Success(uploadId, startTime, totalBytes, uri,"", ""))
        }.apply {
            jobs[uploadId] = this
            invokeOnCompletion {
                if(it is CancellationException) scope.launch {  events.emit(UploadEvent.Cancelled(uploadId, startTime, totalBytes, uri)) }
            }
            start()
        }

        return uploadId
    }

    override fun cancel(uploadId: String) { jobs[uploadId]?.cancel() }
}