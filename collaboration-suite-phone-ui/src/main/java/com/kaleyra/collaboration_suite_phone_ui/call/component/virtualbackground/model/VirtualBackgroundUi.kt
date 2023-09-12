package com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.model

sealed class VirtualBackgroundUi(open val id: String) {

    data class Blur(override val id: String): VirtualBackgroundUi(id)

    data class Image(override val id: String): VirtualBackgroundUi(id)

    object None: VirtualBackgroundUi("None")
}