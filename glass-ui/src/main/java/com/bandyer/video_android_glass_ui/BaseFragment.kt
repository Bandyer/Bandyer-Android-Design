package com.bandyer.video_android_glass_ui

import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager

/**
 * BaseFragment. A base class for all the smart glass fragments
 */
abstract class BaseFragment: Fragment(), TouchEventListener, ChatNotificationManager.NotificationListener {

    /**
     * The fragment's view binding
     */
    protected abstract val binding: ViewBinding

    override fun onShow() = binding.root.setAlphaWithAnimation(0f, 100L)

    override fun onExpanded() {
        requireContext()
    }

    override fun onDismiss() = binding.root.setAlphaWithAnimation(1f, 100L)

    override fun onTouch(event: TouchEvent) = false
}