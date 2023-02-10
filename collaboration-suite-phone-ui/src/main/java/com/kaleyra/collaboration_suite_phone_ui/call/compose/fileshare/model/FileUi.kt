package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

import androidx.compose.runtime.Immutable

@Immutable
data class FileUi(
    val name: String,
    val type: Type,
    val size: Long? = null
) {

   @Immutable
    enum class Type {
        Media,
        Archive,
        Miscellaneous
    }
}