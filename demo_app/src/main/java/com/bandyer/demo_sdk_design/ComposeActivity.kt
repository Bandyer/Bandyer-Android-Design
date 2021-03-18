package com.bandyer.demo_sdk_design

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.filesharing.FileShare
import com.google.android.material.composethemeadapter.MdcTheme

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                FileShare {}
            }
        }
    }
}