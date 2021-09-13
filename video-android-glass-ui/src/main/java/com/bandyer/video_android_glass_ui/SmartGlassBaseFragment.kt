package com.bandyer.video_android_glass_ui

import androidx.fragment.app.Fragment

/**
 * SmartGlassBaseFragment. A base class for all the smart glass fragments
 */
abstract class SmartGlassBaseFragment: Fragment(), BandyerSmartGlassTouchEventListener {
    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent) = false
}