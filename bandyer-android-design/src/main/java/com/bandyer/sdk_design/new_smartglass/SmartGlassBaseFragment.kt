package com.bandyer.sdk_design.new_smartglass

import androidx.fragment.app.Fragment
import com.bandyer.sdk_design.new_smartglass.BandyerSmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.BandyerSmartGlassTouchEventListener

/**
 * SmartGlassBaseFragment. A base class for all the smart glass fragments
 */
abstract class SmartGlassBaseFragment: Fragment(), BandyerSmartGlassTouchEventListener {
    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent) = false
}