package com.bandyer.video_android_glass_ui.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * Get the current navigation fragment
 */
internal val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

/**
 * Safe navigate to a given destination
 *
 * @receiver NavController
 * @param direction NavDirections
 * @return Boolean True of the navigation has been successful, false otherwise
 */
internal fun NavController.safeNavigate(direction: NavDirections): Boolean =
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction); true } ?: false