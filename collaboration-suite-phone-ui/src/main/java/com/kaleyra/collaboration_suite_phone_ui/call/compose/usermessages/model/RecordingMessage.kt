package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model

import java.util.UUID

sealed class RecordingMessage(override val id: String) : UserMessage {

    class Started : RecordingMessage(UUID.randomUUID().toString())

    class Stopped : RecordingMessage(UUID.randomUUID().toString())

    class Failed : RecordingMessage(UUID.randomUUID().toString())
}