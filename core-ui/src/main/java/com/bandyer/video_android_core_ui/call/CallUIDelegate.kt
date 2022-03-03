package com.bandyer.video_android_core_ui.call

import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.SharedFlow

interface CallUIDelegate {
    val call: SharedFlow<Call>
    val usersDescription: UsersDescription
}