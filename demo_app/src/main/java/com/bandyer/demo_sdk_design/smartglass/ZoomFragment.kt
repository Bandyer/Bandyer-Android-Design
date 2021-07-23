package com.bandyer.demo_sdk_design.smartglass

import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.volume.SmartGlassZoomFragment

class ZoomFragment: SmartGlassZoomFragment() {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}