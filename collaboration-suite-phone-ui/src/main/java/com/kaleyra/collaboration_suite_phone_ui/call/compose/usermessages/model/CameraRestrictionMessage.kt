package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model

import java.util.UUID

class CameraRestrictionMessage: UserMessage {

    override val id: String = UUID.randomUUID().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraRestrictionMessage

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}