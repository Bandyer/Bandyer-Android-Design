package com.kaleyra.collaboration_suite_phone_ui.call.usermessages.model

import java.util.UUID

sealed class UsbCameraMessage(override val id: String) : UserMessage {

    data class Connected(val name: String) : UsbCameraMessage(UUID.randomUUID().toString())

    object Disconnected : UsbCameraMessage(UUID.randomUUID().toString())

    object NotSupported : UsbCameraMessage(UUID.randomUUID().toString())
}