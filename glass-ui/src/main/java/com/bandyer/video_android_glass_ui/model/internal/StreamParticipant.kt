package com.bandyer.video_android_glass_ui.model.internal

import com.bandyer.collaboration_center.phonebox.CallParticipant
import com.bandyer.collaboration_center.phonebox.Stream

internal data class StreamParticipant(
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream?
)