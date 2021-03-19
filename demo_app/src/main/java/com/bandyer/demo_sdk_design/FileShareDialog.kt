package com.bandyer.demo_sdk_design

import androidx.fragment.app.viewModels
import com.bandyer.sdk_design.filesharing.*

class FileShareDialog: BandyerFileShareDialogFragment() {

    private val viewModel: FileShareViewModel by viewModels()

    override fun getSharedFiles() = viewModel.fileShareItems

    override fun onAddButtonPressed() { viewModel.addItem() }

    override fun onItemEvent(event: FileShareItemEvent) { viewModel.updateItem(event.item) }

    override fun onItemButtonEvent(event: FileShareItemButtonEvent) {
        when(event) {
            is FileShareItemButtonEvent.Cancel -> viewModel.removeItem(event.item)
            is FileShareItemButtonEvent.Retry -> viewModel.removeItem(event.item)
            is FileShareItemButtonEvent.Download -> viewModel.removeItem(event.item)
        }
    }
}