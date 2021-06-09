package com.bandyer.demo_sdk_design

import android.os.Bundle
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

class FileShareActivity : AppCompatActivity() {

    private val viewModel: LocalFileShareViewModel by viewModels()

    private var fileShareDialog: BandyerFileShareDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)

        fileShareDialog = BandyerFileShareDialog()

        initListeners()

        lifecycleScope.launchWhenStarted {
            viewModel.uploadEvents.collect {
                when(it) {
                    is UploadState.Pending -> viewModel.itemsData[it.uploadId] = UploadData.Pending(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.OnProgress -> viewModel.itemsData[it.uploadId] = UploadData.OnProgress(it.uploadId, it.startTime, it.totalBytes, it.fileUri, it.uploadedBytes)
                    is UploadState.Success -> viewModel.itemsData[it.uploadId] = UploadData.Success(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.Error -> viewModel.itemsData[it.uploadId] = UploadData.Error(it.uploadId, it.startTime, it.totalBytes, it.fileUri)
                    is UploadState.Cancelled -> viewModel.itemsData.remove(it.uploadId)
                }
                if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(viewModel.itemsData)
            }
        }

        lifecycleScope.launchWhenStarted {
           viewModel.downloadEvents.collect {
               val sender = viewModel.itemsData[it.downloadId]?.let { itemData -> (itemData as DownloadItemData).sender } ?: ""
               when(it) {
                   is DownloadState.Pending -> viewModel.itemsData[it.downloadId] = DownloadData.Pending(it.downloadId, it.endpoint, it.startTime, it.totalBytes, sender, "".toUri())
                   is DownloadState.OnProgress -> viewModel.itemsData[it.downloadId] = DownloadData.OnProgress(it.downloadId, it.endpoint, it.startTime, it.totalBytes, it.downloadBytes, sender, "".toUri())
                   is DownloadState.Success -> viewModel.itemsData[it.downloadId] = DownloadData.Success(it.downloadId, it.endpoint, it.startTime, it.totalBytes, sender, "".toUri())
                   is DownloadState.Error -> viewModel.itemsData[it.downloadId] = DownloadData.Error(it.downloadId, it.endpoint, it.startTime, it.totalBytes, it.throwable, sender, "".toUri())
                   is DownloadState.Cancelled -> viewModel.itemsData.remove(it.downloadId)
               }
               if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(viewModel.itemsData)
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

    private fun initListeners() {
        findViewById<MaterialButton>(R.id.btn_add_upload).setOnClickListener {
            viewModel.upload(uploadId = null, context = this, uri = "".toUri())
        }

        findViewById<MaterialButton>(R.id.btn_add_download).setOnClickListener {
            viewModel.download(downloadId = null, endpoint = "", context = this)
        }

        findViewById<MaterialButton>(R.id.btn_add_download_available).setOnClickListener {
            val downloadId = UUID.randomUUID().toString()
            viewModel.itemsData[downloadId] = DownloadAvailableData(id = downloadId, sender = "Will Smith", endpoint = "", startTime = Date().time, totalBytes = 0L)
            if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(viewModel.itemsData)
        }

        findViewById<MaterialButton>(R.id.btn_file_share).setOnClickListener {
            fileShareDialog?.show(this@FileShareActivity, viewModel)
        }
    }
}