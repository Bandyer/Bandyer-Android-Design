package com.bandyer.demo_sdk_design

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bandyer.sdk_design.filesharing.FileShare
import com.bandyer.sdk_design.filesharing.FileShareComposeExperimentalTheme
import com.google.android.material.composethemeadapter.MdcTheme

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    FileShare() {}
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun FileSharePreview() {
    FileShareComposeExperimentalTheme {
        FileShare() {}
    }
}