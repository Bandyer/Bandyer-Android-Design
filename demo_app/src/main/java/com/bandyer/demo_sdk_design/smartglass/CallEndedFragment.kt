package com.bandyer.demo_sdk_design.smartglass

import com.bandyer.sdk_design.new_smartglass.SmartGlassCallEndedFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class CallEndedFragment: SmartGlassCallEndedFragment() {

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when (event) {
        SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
            requireActivity().finish()
            true
        }
        else -> false
    }
}