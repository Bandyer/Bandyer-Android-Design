package com.bandyer.video_android_core_ui.call

import com.bandyer.video_android_core_ui.common.BoundServiceActivity

abstract class CallActivity: BoundServiceActivity<CallService>(CallService::class.java)