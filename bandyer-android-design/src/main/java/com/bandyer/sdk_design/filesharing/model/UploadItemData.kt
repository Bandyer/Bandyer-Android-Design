package com.bandyer.sdk_design.filesharing.model

import android.net.Uri

interface UploadItemData: FileShareItemData {
    override val uri: Uri
}