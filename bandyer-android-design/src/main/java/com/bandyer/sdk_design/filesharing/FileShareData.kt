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
data class FileShareData(val operationType: FILE_SHARE_OP_TYPE,
                         val fileName: String,
                         val fileType: String,
                         val fileSize: Long,
                         val fileUrl: String,
                         val sender: String)

enum class FILE_SHARE_OP_TYPE {
    UPLOAD,
    DOWNLOAD
}