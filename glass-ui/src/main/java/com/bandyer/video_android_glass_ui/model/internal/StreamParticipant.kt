package com.bandyer.video_android_glass_ui.model.internal

import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.model.Stream

internal data class StreamParticipant(
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream?
)