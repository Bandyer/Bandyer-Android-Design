package com.bandyer.sdk_design.filesharing.model

interface DownloadItemData: FileShareItemData {
    val endpoint: String
    val sender: String
}