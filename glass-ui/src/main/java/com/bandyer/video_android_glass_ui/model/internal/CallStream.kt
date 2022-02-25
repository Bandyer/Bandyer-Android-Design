package com.bandyer.video_android_glass_ui.model.internal

import android.net.Uri
import android.view.View
import com.bandyer.collaboration_center.phonebox.CallParticipant
import com.bandyer.collaboration_center.phonebox.Stream

internal data class CallStream(
    val participant: CallParticipant,
    val isMyStream: Boolean,
    val stream: Stream
)

internal data class StreamItemData(
    val isMyStream: Boolean,
    val userDescription: String,
    val userImage: Uri,
    val isAudioEnabled: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val view: View? = null
)