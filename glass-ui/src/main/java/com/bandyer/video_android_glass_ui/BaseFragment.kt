package com.bandyer.video_android_glass_ui

import androidx.fragment.app.Fragment

/**
 * BaseFragment. A base class for all the smart glass fragments
 */
abstract class BaseFragment: Fragment(), TouchEventListener {
    override fun onTouch(event: TouchEvent) = false
}