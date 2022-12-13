package com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model

data class FileUi(
    val name: String,
    val type: Type,
    val size: Long? = null
) {
    enum class Type {
        Media,
        Archive,
        Miscellaneous
    }
}