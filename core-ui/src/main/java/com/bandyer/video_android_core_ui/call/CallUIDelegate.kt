package com.bandyer.video_android_core_ui.call

import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_core_ui.model.UsersDescription

interface CallUIDelegate {
    val call: Call
    val usersDescription: UsersDescription
}