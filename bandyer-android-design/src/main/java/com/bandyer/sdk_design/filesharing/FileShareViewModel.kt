package com.bandyer.sdk_design.filesharing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bandyer.sdk_design.filesharing.model.TransferData
import java.util.*

/**
 * View model for the file sharing
 */
abstract class FileShareViewModel : ViewModel() {

    /**
     * A map of [TransferData] which keeps track of the transferred files
     */
    abstract val itemsData: Map<String, TransferData>

    /**
     * Upload a file
     *
     * @param context Context
     * @param id The id of the upload, by default it's random
     * @param uri The uri of the file to upload
     * @param sender The sender name
     */
    abstract fun uploadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)


    /**
     * Download a file from the given uri
     *
     * @param context Context
     * @param id The id of the download, by default it's random
     * @param uri The server's uri where a file is available
     * @param sender The sender name
     */
    abstract fun downloadFile(context: Context, id: String = UUID.randomUUID().toString(), uri: Uri, sender: String)

    /**
     * Cancel the upload with the given id
     *
     * @param uploadId The upload id
     */
    abstract fun cancelFileUpload(uploadId: String)

    /**
     * Cancel the download with the given id
     *
     * @param downloadId The download id
     */
    abstract fun cancelFileDownload(downloadId: String)

    /**
     * Cancel all the pending/ongoing uploads
     */
    abstract fun cancelAllFileUploads()

    /**
     * Cancel all the pending/ongoing downloads
     */
    abstract fun cancelAllFileDownloads()
}
