package com.bandyer.sdk_design.new_smartglass

import androidx.fragment.app.Fragment

abstract class SmartGlassBaseFragment: Fragment(), SmartGlassTouchEventListener {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent) = false
}