package com.bandyer.video_android_glass_ui.model.internal

import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.model.Stream

internal data class StreamParticipant(
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StreamParticipant) return false

        if (participant.id != other.participant.id) return false
        if (stream?.id != other.stream?.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = participant.id.hashCode()
        result = 31 * result + stream?.id.hashCode()
        return result
    }
}