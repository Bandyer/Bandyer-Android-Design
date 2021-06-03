package com.bandyer.sdk_design.filesharing

interface DownloadItemData: FileShareItemData {
    val endpoint: String
    val sender: String
}