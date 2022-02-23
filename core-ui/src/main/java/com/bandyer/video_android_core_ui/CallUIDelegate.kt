package com.bandyer.video_android_core_ui

import com.bandyer.collaboration_center.phonebox.Call
import kotlinx.coroutines.flow.SharedFlow

interface CallUIDelegate {
    val call: SharedFlow<Call>
    val usersDescription: UsersDescription
}