package com.bandyer.demo_sdk_design.smartglass

import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassEndCallFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class EndCallFragment: SmartGlassEndCallFragment() {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = when (event) {
        SmartGlassTouchEvent.Event.TAP -> {
            findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
            true
        }
        SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> false
    }
}