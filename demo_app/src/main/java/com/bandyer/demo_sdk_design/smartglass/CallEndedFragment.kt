package com.bandyer.demo_sdk_design.smartglass

import com.bandyer.sdk_design.new_smartglass.SmartGlassCallEndedFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class CallEndedFragment: SmartGlassCallEndedFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.TAP, SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            requireActivity().finish()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}