package com.bandyer.demo_sdk_design

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bandyer.demo_sdk_design.file_share.LocalFileShareViewModel
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog
import com.google.android.material.button.MaterialButton

class FileShareActivity : AppCompatActivity() {

    val viewModel: LocalFileShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)

        findViewById<MaterialButton>(R.id.btn_file_share).setOnClickListener {
            BandyerFileShareDialog().show(this@FileShareActivity)
        }
    }
}