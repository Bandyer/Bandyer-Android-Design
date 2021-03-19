package com.bandyer.sdk_design.filesharing

/**
* File information
* @property fileName name of file
* @property fileType type of file
* @property fileSize size of file
* @property sender sender userAlias
*/
data class FileShareItemData(var id: Int,
                             var fileName: String,
                             var progress: Float,
                             var isUpload: Boolean,
                             var sender: String,
                             var fileType: FileType,
                             var fileSize: Long): FileShareStyle

enum class FileType {
    MISC,
    MEDIA,
    ARCHIVE
}