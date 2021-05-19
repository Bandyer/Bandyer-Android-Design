package com.bandyer.sdk_design.filesharing

/**
* File information
* @property fileName name of file
* @property fileType type of file
* @property fileSize size of file
* @property sender sender userAlias
*/
data class FileShareData(val isUpload: Boolean,
                         val fileName: String,
                         val fileType: FileType,
                         val fileSize: Long,
                         val progress: Int,
                         val sender: String)

enum class FileType {
    FILE,
    IMAGE,
    ARCHIVE
}
