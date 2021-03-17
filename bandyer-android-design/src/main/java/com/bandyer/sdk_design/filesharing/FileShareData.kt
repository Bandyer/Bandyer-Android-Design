package com.bandyer.sdk_design.filesharing

/**
* File information
* @property operationType type of operation
* @property fileName name of file
* @property fileType type of file
* @property fileSize size of file
* @property fileUrl url of file
* @property sender sender userAlias
*/
data class FileShareData(var fileName: String,
                         var isUpload: Boolean,
                         var sender: String,
                         var fileType: FileType,
                         var fileSize: Long)

enum class FileType {
    FILE,
    MEDIA,
    ARCHIVE
}