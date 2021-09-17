package com.bandyer.video_android_glass_ui

import androidx.fragment.app.Fragment

/**
 * BandyerGlassBaseFragment. A base class for all the smart glass fragments
 */
abstract class BandyerGlassBaseFragment: Fragment(), BandyerGlassTouchEventListener {
    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent) = false
}