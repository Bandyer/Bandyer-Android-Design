package com.bandyer.video_android_glass_ui.model.internal

import android.net.Uri
import com.bandyer.collaboration_center.phonebox.CallParticipant
import com.bandyer.collaboration_center.phonebox.Stream

internal data class StreamParticipant(
    val id: String,
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream?,
    val userDescription: String,
    val userImage: Uri
)