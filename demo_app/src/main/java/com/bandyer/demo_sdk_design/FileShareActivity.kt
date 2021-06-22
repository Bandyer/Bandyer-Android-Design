package com.bandyer.demo_sdk_design

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.demo_sdk_design.databinding.ActivityFileShareBinding
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog

class FileShareActivity : AppCompatActivity() {

    private var fileShareDialog: BandyerFileShareDialog? = null

    private lateinit var binding: ActivityFileShareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileShareDialog = BandyerFileShareDialog()

        initListeners()
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
        binding.btnAddUpload.setOnClickListener { }

        binding.btnAddDownload.setOnClickListener { }

        binding.btnAddDownloadAvailable.setOnClickListener {
//            viewModel.itemsData[downloadId] = DownloadAvailableData(id = downloadId, sender = "Will Smith", endpoint = "", startTime = Date().time, totalBytes = 0L, fileName = "")
//            if(fileShareDialog?.isVisible == true) fileShareDialog?.updateRecyclerViewItems(viewModel.itemsData)
        }

        binding.btnFileShare.setOnClickListener {
            fileShareDialog?.show(this@FileShareActivity)
        }
    }
}