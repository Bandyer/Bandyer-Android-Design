package com.bandyer.demo_sdk_design

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog

class FileShareActivity : AppCompatActivity() {

    private var fileShareDialog: BandyerFileShareDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)

        fileShareDialog = BandyerFileShareDialog()

    }

    override fun onStop() {
        super.onStop()
        fileShareDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileShareDialog = null
    }
}