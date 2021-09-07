package com.bandyer.sdk_design.new_smartglass.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Get the current navigation fragment
 */
val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()