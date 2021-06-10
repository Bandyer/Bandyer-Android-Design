package com.bandyer.sdk_design.filesharing

import android.net.Uri

sealed class DownloadData(override val id: String, override val endpoint: String, override val startTime: Long, override val totalBytes: Long, override val sender: String, override val uri: Uri,
                          override val fileName: String): DownloadItemData {
    class Pending(id: String, endpoint: String, startTime: Long, totalBytes: Long, sender: String, uri: Uri, fileName: String) : DownloadData(id, endpoint, startTime, totalBytes, sender, uri, fileName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Pending) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Success(id: String, endpoint: String, startTime: Long, totalBytes: Long, sender: String, uri: Uri, fileName: String) : DownloadData(id, endpoint, startTime, totalBytes, sender, uri, fileName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Error(id: String, endpoint: String, startTime: Long, totalBytes: Long, val throwable: Throwable, sender: String, uri: Uri, fileName: String) : DownloadData(id, endpoint, startTime, totalBytes, sender, uri, fileName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (!super.equals(other)) return false

            if (throwable != other.throwable) return false

            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode() + throwable.hashCode()
    }

    class OnProgress(id: String, endpoint: String, startTime: Long, totalBytes: Long, val downloadBytes: Long, sender: String, uri: Uri, fileName: String) : DownloadData(id, endpoint, startTime, totalBytes, sender, uri, fileName) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OnProgress) return false
            if (!super.equals(other)) return false

            if (downloadBytes != other.downloadBytes) return false

            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode() + downloadBytes.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadData) return false

        if (id != other.id) return false
        if (endpoint != other.endpoint) return false
        if (startTime != other.startTime) return false
        if (totalBytes != other.totalBytes) return false
        if (uri != other.uri) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int = this::class.java.simpleName.hashCode() + id.hashCode() + endpoint.hashCode() + startTime.hashCode() + totalBytes.hashCode() + uri.hashCode() + fileName.hashCode()
}