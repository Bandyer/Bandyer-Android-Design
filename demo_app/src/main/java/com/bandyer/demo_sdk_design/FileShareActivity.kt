package com.bandyer.demo_sdk_design

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bandyer.communication_center.file_share.file_sharing_center.UploadState
import com.bandyer.communication_center.file_share.file_sharing_center.request.DownloadState
import com.bandyer.demo_sdk_design.file_share.LocalFileShareViewModel
import com.bandyer.sdk_design.filesharing.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collect
import java.io.File
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
                    is UploadState.Pending -> itemsData[it.uploadId] = UploadData.Pending(it.uploadId, it.startTime, it.totalBytes, it.file)
                    is UploadState.OnProgress -> itemsData[it.uploadId] = UploadData.OnProgress(it.uploadId, it.startTime, it.totalBytes, it.file, it.uploadedBytes)
                    is UploadState.Success -> itemsData[it.uploadId] = UploadData.Success(it.uploadId, it.startTime, it.totalBytes, it.file)
                    is UploadState.Error -> itemsData[it.uploadId] = UploadData.Error(it.uploadId, it.startTime, it.totalBytes, it.file)
                    is UploadState.Cancelled -> itemsData.remove(it.uploadId)
                }
                if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(itemsData)
            }
        }

        lifecycleScope.launchWhenStarted {
           viewModel.downloadEvents.collect {
               when(it) {
                   is DownloadState.Pending -> itemsData[it.downloadId] = DownloadData.Pending(it.downloadId, it.endpoint, it.file, it.startTime, it.totalBytes, "John Smith")
                   is DownloadState.OnProgress -> itemsData[it.downloadId] = DownloadData.OnProgress(it.downloadId, it.endpoint, it.file, it.startTime, it.totalBytes, it.downloadBytes, "John Smith")
                   is DownloadState.Success -> itemsData[it.downloadId] = DownloadData.Success(it.downloadId, it.endpoint, it.file, it.startTime, it.totalBytes, "John Smith")
                   is DownloadState.Error -> itemsData[it.downloadId] = DownloadData.Error(it.downloadId, it.endpoint, it.file, it.startTime, it.totalBytes, it.throwable, "John Smith")
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

    private fun initListeners() {
        findViewById<MaterialButton>(R.id.btn_add_upload).setOnClickListener {
            viewModel.upload(file = File(""))
        }

        findViewById<MaterialButton>(R.id.btn_add_download).setOnClickListener {
            viewModel.download(file = File(""), endpoint = "")
        }

        findViewById<MaterialButton>(R.id.btn_file_share).setOnClickListener {
            fileShareDialog?.show(this@FileShareActivity, viewModel, itemsData)
        }
    }
}