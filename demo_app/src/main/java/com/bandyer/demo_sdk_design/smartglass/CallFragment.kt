package com.bandyer.demo_sdk_design.smartglass

import com.bandyer.sdk_design.new_smartglass.SmartGlassCallFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class CallFragment: SmartGlassCallFragment() {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false
}