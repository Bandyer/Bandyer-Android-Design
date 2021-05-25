package com.bandyer.sdk_design.filesharing

import java.io.File

data class DownloadAvailableData(
    val downloadId: String,
    val sender: String,
    val endpoint: String,
    val file: File,
    val startTime: Long
): FileShareItemData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadAvailableData) return false

        if (downloadId != other.downloadId) return false
        if (sender != other.sender) return false
        if (endpoint != other.endpoint) return false
        if (file != other.file) return false
        if (startTime != other.startTime) return false

        return true
    }

    override fun hashCode(): Int = this::class.java.simpleName.hashCode() + endpoint.hashCode() + file.hashCode() + downloadId.hashCode() + startTime.hashCode() + sender.hashCode()
}