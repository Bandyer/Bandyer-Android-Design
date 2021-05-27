package com.bandyer.sdk_design.filesharing

import android.net.Uri
import java.io.File

sealed class UploadData(val uploadId: String, val startTime: Long, val totalBytes: Long, val uri: Uri): FileShareItemData {
    class Pending(uploadId: String, startTime: Long, totalBytes: Long, uri: Uri) : UploadData(uploadId, startTime, totalBytes, uri) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Pending) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Success(uploadId: String, startTime: Long, totalBytes: Long, uri: Uri) : UploadData(uploadId, startTime, totalBytes, uri) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class Error(uploadId: String, startTime: Long, totalBytes: Long, uri: Uri) : UploadData(uploadId, startTime, totalBytes, uri) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Error) return false
            if (!super.equals(other)) return false
            return true
        }
        
        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode()
    }

    class OnProgress(uploadId: String, startTime: Long, totalBytes: Long, uri: Uri, val uploadedBytes: Long) : UploadData(uploadId, startTime, totalBytes, uri) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OnProgress) return false
            if (!super.equals(other)) return false

            if (uploadedBytes != other.uploadedBytes) return false

            return true
        }
        
        override fun hashCode(): Int = super.hashCode() + this::class.java.simpleName.hashCode() + uploadedBytes.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadData) return false

        if (uploadId != other.uploadId) return false
        if (startTime != other.startTime) return false
        if (totalBytes != other.totalBytes) return false
        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int = this::class.java.simpleName.hashCode() + uploadId.hashCode() + startTime.hashCode() + totalBytes.hashCode() + uri.hashCode()
}