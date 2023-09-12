package com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model

import java.util.UUID

sealed class AudioConnectionFailureMessage(override val id: String) : UserMessage {

    object Generic : AudioConnectionFailureMessage(UUID.randomUUID().toString())

    object InSystemCall : AudioConnectionFailureMessage(UUID.randomUUID().toString())

}