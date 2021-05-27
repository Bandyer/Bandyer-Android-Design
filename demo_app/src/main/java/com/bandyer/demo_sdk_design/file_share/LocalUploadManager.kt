package com.bandyer.demo_sdk_design.file_share

import android.content.Context
import android.net.Uri
import com.bandyer.communication_center.file_share.file_sharing_center.FileSharingConfig
import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpStack
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpUploader
import com.bandyer.communication_center.file_share.file_sharing_center.request.invokeOnCancelled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LocalUploadManager private constructor(override val httpStack: HttpStack, override val scope: CoroutineScope): HttpUploader {

    companion object {
        fun newInstance(config: FileSharingConfig = FileSharingConfig()): HttpUploader = LocalUploadManager(config.httpStack, config.ioScope)
    }

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val uploads: StateFlow<HashMap<String, UploadState>> = MutableStateFlow(hashMapOf())

    override val events = MutableSharedFlow<UploadState>()

    override fun upload(uploadId: String, context: Context, uri: Uri): String {
        val startTime = Date().time
        val nOfUpdates = 10
        val file = try { FileUtils.from(context, uri) } catch (ex: Exception) { File("") }
        val totalBytes = if(file.length() != 0L) file.length() else 100L

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(UploadState.Pending(uploadId, startTime, totalBytes, uri))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(UploadState.OnProgress(uploadId, startTime, totalBytes, uri,(totalBytes / nOfUpdates) * i))
            }
            events.emit(UploadState.Success(uploadId, startTime, totalBytes, uri,"", ""))
        }.apply {
            jobs[uploadId] = this
            invokeOnCancelled(scope.coroutineContext) {
                events.emit(UploadState.Cancelled(uploadId, startTime, totalBytes, uri))
            }
            start()
        }

        return uploadId
    }

    override fun cancel(uploadId: String) { jobs[uploadId]?.cancel() }
}