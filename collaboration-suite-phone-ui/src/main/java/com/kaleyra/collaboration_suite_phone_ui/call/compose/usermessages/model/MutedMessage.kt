package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model

import java.util.UUID

class MutedMessage(val admin: String?) : UserMessage {

    override val id: String = UUID.randomUUID().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MutedMessage

        if (admin != other.admin) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = admin?.hashCode() ?: 0
        result = 31 * result + id.hashCode()
        return result
    }
}