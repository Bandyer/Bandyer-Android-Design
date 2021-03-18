package com.bandyer.demo_sdk_design

import com.bandyer.sdk_design.filesharing.*

class FileShareDialog: BandyerFileShareDialogFragment() {

    override fun onAddButtonPressed() = Unit

    override fun onItemEvent(event: FileShareItemEvent) = Unit

    override fun onItemButtonEvent(event: FileShareItemButtonEvent) = Unit
}