package com.bandyer.demo_sdk_design

import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.networking.models.AmazonTransferPolicy
import com.bandyer.communication_center.file_share.file_sharing_center.request.AmazonUploader
import com.bandyer.communication_center.file_share.file_sharing_center.request.HttpStack
import com.bandyer.communication_center.file_share.file_sharing_center.request.invokeOnCancelled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MockUploadManager(override val httpStack: HttpStack, override val scope: CoroutineScope): AmazonUploader {

    private val jobs: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

    override val uploads: StateFlow<HashMap<String, UploadState>> = MutableStateFlow(hashMapOf())

    override val events = MutableSharedFlow<UploadState>()

    override fun upload(
        uploadId: String,
        policy: AmazonTransferPolicy,
        file: File,
        keepFileOnSuccess: Boolean
    ): String {
        val startTime = Date().time
        val totalBytes = 100L
        val nOfUpdates = 10

        scope.launch(start = CoroutineStart.LAZY) {
            events.emit(UploadState.Pending(uploadId, startTime, totalBytes, file))
            for(i in 0..nOfUpdates) {
                delay(500)
                if(!isActive) break
                events.emit(UploadState.OnProgress(uploadId, startTime, totalBytes, file,(totalBytes / nOfUpdates) * i))
            }
            events.emit(UploadState.Success(uploadId, startTime, totalBytes, file, "", ""))
        }.apply {
            jobs[uploadId] = this
            invokeOnCancelled(scope.coroutineContext) {
                events.emit(UploadState.Cancelled(uploadId, startTime, totalBytes, file))
            }
            start()
        }

        return uploadId
    }

    override fun cancel(uploadId: String) { jobs[uploadId]?.cancel() }
}