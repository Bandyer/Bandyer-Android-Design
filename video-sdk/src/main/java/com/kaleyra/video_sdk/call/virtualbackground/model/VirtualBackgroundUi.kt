package com.kaleyra.video_sdk.call.virtualbackground.model

sealed class VirtualBackgroundUi(open val id: String) {

    data class Blur(override val id: String): VirtualBackgroundUi(id)

    data class Image(override val id: String): VirtualBackgroundUi(id)

    object None: VirtualBackgroundUi("None")
}