package com.bandyer.demo_sdk_design

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.concurrent.ConcurrentHashMap

class FileShareActivity : AppCompatActivity() {

//    private var adapterItems: ConcurrentHashMap<String, CustomAbstractItem<*, *>> = ConcurrentHashMap()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_share)
    }
}