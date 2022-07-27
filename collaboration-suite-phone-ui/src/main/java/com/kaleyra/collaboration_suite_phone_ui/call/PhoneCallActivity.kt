package com.kaleyra.collaboration_suite_phone_ui.call

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import com.google.android.material.composethemeadapter.MdcTheme

class PhoneCallActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                Text("Dummy Call Activity")
            }
        }
    }
}