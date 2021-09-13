package com.bandyer.video_android_glass_ui.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Get the current navigation fragment
 */
val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()