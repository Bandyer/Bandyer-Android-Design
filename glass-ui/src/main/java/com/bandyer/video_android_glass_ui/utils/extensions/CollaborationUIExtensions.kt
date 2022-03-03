package com.bandyer.video_android_glass_ui.utils.extensions

import com.bandyer.collaboration_center.Collaboration
import com.bandyer.video_android_core_ui.CollaborationUI
import com.bandyer.video_android_glass_ui.GlassCallActivity

fun CollaborationUI.setUpWithGlassUI(
    credentials: Collaboration.Credentials,
    configuration: Collaboration.Configuration
) = setUp(credentials, configuration, GlassCallActivity::class.java)
