package com.bandyer.demo_sdk_design

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.demo_sdk_design.file_share.LocalFileShareViewModel
import com.bandyer.sdk_design.filesharing.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collect
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FileShareActivity : AppCompatActivity() {

    private val viewModel: LocalFileShareViewModel by viewModels()

    private val itemsData: ConcurrentHashMap<String, FileShareItemData> = ConcurrentHashMap()

    private var fileShareDialog: BandyerFileShareDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)

        fileShareDialog = BandyerFileShareDialog()

        initListeners()

        lifecycleScope.launchWhenStarted {
            viewModel.uploadEvents.collect {
                when(it) {
                    is UploadState.Pending -> itemsData[it.uploadId] = UploadData.Pending(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.OnProgress -> itemsData[it.uploadId] = UploadData.OnProgress(it.uploadId, it.startTime, it.totalBytes, it.fileUri, it.uploadedBytes)
                    is UploadState.Success -> itemsData[it.uploadId] = UploadData.Success(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.Error -> itemsData[it.uploadId] = UploadData.Error(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.Cancelled -> itemsData.remove(it.uploadId)
                }
                if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(itemsData)
            }
        }

        lifecycleScope.launchWhenStarted {
           viewModel.downloadEvents.collect {
               val sender = itemsData[it.downloadId]?.let { itemData -> (itemData as DownloadItemData).sender } ?: ""
               when(it) {
                   is DownloadState.Pending -> itemsData[it.downloadId] = DownloadData.Pending(it.downloadId, it.endpoint, it.startTime, it.totalBytes, sender, "".toUri())
                   is DownloadState.OnProgress -> itemsData[it.downloadId] = DownloadData.OnProgress(it.downloadId, it.endpoint, it.startTime, it.totalBytes, it.downloadBytes, sender, "".toUri())
                   is DownloadState.Success -> itemsData[it.downloadId] = DownloadData.Success(it.downloadId, it.endpoint, it.startTime, it.totalBytes, sender, "".toUri())
                   is DownloadState.Error -> itemsData[it.downloadId] = DownloadData.Error(it.downloadId, it.endpoint, it.startTime, it.totalBytes, it.throwable, sender, "".toUri())
                   is DownloadState.Cancelled -> itemsData.remove(it.downloadId)
               }
               if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(itemsData)
           }
        }
    }

    override fun onStop() {
        super.onStop()
        fileShareDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileShareDialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return

            viewModel.upload(uploadId = null, context = this, uri = uri)
        }
    }

    private fun initListeners() {
        findViewById<MaterialButton>(R.id.btn_add_upload).setOnClickListener {
            viewModel.upload(uploadId = null, context = this, uri = "".toUri())
        }

        findViewById<MaterialButton>(R.id.btn_add_download).setOnClickListener {
            viewModel.download(downloadId = null, endpoint = "", context = this)
        }

        findViewById<MaterialButton>(R.id.btn_add_download_available).setOnClickListener {
            val downloadId = UUID.randomUUID().toString()
            itemsData[downloadId] = DownloadAvailableData(id = downloadId, sender = "Will Smith", endpoint = "", startTime = Date().time, totalBytes = 0L)
            fileShareDialog?.updateRecyclerViewItems(itemsData)
        }

        findViewById<MaterialButton>(R.id.btn_file_share).setOnClickListener {
            fileShareDialog?.show(this@FileShareActivity, viewModel, itemsData) {
                val fileintent = Intent(Intent.ACTION_GET_CONTENT)
                fileintent.type = "*/*"
                fileintent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    startActivityForResult(fileintent, FILE_REQUEST_CODE)
                } catch (e: ActivityNotFoundException) {
                    Log.e(
                        this.javaClass.name,
                        "No activity can handle picking a file. Showing alternatives."
                    )
                }
            }
        }
    }

    companion object {
        const val FILE_REQUEST_CODE = 654
    }
}