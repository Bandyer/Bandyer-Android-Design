package com.bandyer.demo_sdk_design

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.bandyer.sdk_design.filesharing.*
import com.google.android.material.composethemeadapter.MdcTheme

class ComposeActivity: AppCompatActivity() {

    private val viewModel: FileShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                FileShare(sharedFiles = viewModel.fileShareItems,
                    onNavIconPressed = { onBackPressed() },
                    onAddButtonPressed = { viewModel.addItem() },
                    onItemEvent = { event -> viewModel.updateItem(event.item) },
                    onItemButtonEvent = { event -> when(event) {
                        is FileShareItemButtonEvent.Cancel -> viewModel.removeItem(event.item)
                        is FileShareItemButtonEvent.Retry -> viewModel.removeItem(event.item)
                        is FileShareItemButtonEvent.Download -> viewModel.removeItem(event.item)
                    } }
                )
            }
        }
    }

}