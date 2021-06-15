package com.bandyer.sdk_design.filesharing.model

import android.net.Uri

data class DownloadAvailableData(
    override val id: String,
    override val endpoint: String,
    override val startTime: Long,
    override val totalBytes: Long,
    override val sender: String,
    override val uri: Uri? = null,
    override val fileName: String
): DownloadItemData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadAvailableData) return false

        if (id != other.id) return false
        if (endpoint != other.endpoint) return false
        if (startTime != other.startTime) return false
        if (totalBytes != other.totalBytes) return false
        if (sender != other.sender) return false
        if (uri != other.uri) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int = this::class.java.simpleName.hashCode() + endpoint.hashCode() + id.hashCode() + startTime.hashCode() + sender.hashCode() + totalBytes.hashCode() + uri.hashCode() + fileName.hashCode()

}