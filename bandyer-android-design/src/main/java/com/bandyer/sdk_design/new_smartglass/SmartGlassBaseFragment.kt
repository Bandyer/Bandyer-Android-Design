package com.bandyer.sdk_design.new_smartglass

import androidx.fragment.app.Fragment

/**
 * SmartGlassBaseFragment. A base glass for all the SmartGlassXxxFragments
 */
abstract class SmartGlassBaseFragment: Fragment(), SmartGlassTouchEventListener {
    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent) = false
}