package com.bandyer.sdk_design.filesharing

import java.io.File

sealed class DownloadState(val downloadId: String, val endpoint: String, val file: File, val startTime: Long, val totalBytes: Long, val sender: String) {
    class Pending(downloadId: String, endpoint: String, file: File, startTime: Long, totalBytes: Long, sender: String) : DownloadState(downloadId, endpoint, file, startTime, totalBytes, sender) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Pending) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Success(downloadId: String, endpoint: String, file: File, startTime: Long, totalBytes: Long, sender: String) : DownloadState(downloadId, endpoint, file, startTime, totalBytes, sender) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Error(downloadId: String, endpoint: String, file: File, startTime: Long, totalBytes: Long, val throwable: Throwable, sender: String) : DownloadState(downloadId, endpoint, file, startTime, totalBytes, sender) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (!super.equals(other)) return false

            if (throwable != other.throwable) return false

            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode() + throwable.hashCode()
    }

    class OnProgress(downloadId: String, endpoint: String, file: File, startTime: Long, totalBytes: Long, val downloadBytes: Long, sender: String) : DownloadState(downloadId, endpoint, file, startTime, totalBytes, sender) {
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
        if (other !is DownloadState) return false

        if (downloadId != other.downloadId) return false
        if (endpoint != other.endpoint) return false
        if (file != other.file) return false
        if (startTime != other.startTime) return false
        if (totalBytes != other.totalBytes) return false

        return true
    }

    override fun hashCode(): Int = this::class.java.simpleName.hashCode() + downloadId.hashCode() + endpoint.hashCode() + file.hashCode() + startTime.hashCode() + totalBytes.hashCode()
}