package com.bandyer.demo_sdk_design.smartglass

import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.volume.SmartGlassVolumeFragment

class VolumeFragment: SmartGlassVolumeFragment() {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.TAP, SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}