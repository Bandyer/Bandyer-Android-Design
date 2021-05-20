package com.bandyer.demo_sdk_design

import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.networking.models.AmazonTransferPolicy
import com.bandyer.communication_center.file_share.file_sharing_center.request.Uploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.HashMap

class MockUploadManager: Uploader<AmazonTransferPolicy> {

    override val uploads: StateFlow<HashMap<String, UploadState>>
        get() = TODO("Not yet implemented")
    override val scope: CoroutineScope
        get() = TODO("Not yet implemented")
    override val events: SharedFlow<UploadState>
        get() = TODO("Not yet implemented")

    override fun upload(
        uploadId: String,
        policy: AmazonTransferPolicy,
        file: File,
        keepFileOnSuccess: Boolean
    ): String {

    }

    override fun cancel(uploadId: String) {
        TODO("Not yet implemented")
    }
}