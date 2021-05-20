package com.bandyer.demo_sdk_design

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bandyer.communication_center.file_share.file_sharing_center.FileSharingConfig
import com.bandyer.demo_sdk_design.file_share.FileShareViewModel
import com.bandyer.demo_sdk_design.file_share.MockFileSharingManager
import com.bandyer.sdk_design.filesharing.BandyerFileShareDialog
import com.google.android.material.button.MaterialButton

class FileShareActivity : AppCompatActivity() {

//    private var adapterItems: ConcurrentHashMap<String, CustomAbstractItem<*, *>> = ConcurrentHashMap()
    private val fsm = MockFileSharingManager.newInstance(FileSharingConfig(""))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)

        findViewById<MaterialButton>(R.id.btn_file_share).setOnClickListener {
            BandyerFileShareDialog().show(this@FileShareActivity)
        }
    }
}