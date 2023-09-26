package com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model

import java.util.UUID

sealed class RecordingMessage(override val id: String) : UserMessage {

    object Started : RecordingMessage(UUID.randomUUID().toString())

    object Stopped : RecordingMessage(UUID.randomUUID().toString())

    object Failed : RecordingMessage(UUID.randomUUID().toString())
}