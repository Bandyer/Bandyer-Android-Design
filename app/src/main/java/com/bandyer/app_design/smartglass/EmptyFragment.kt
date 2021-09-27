package com.bandyer.app_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.app_design.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.call.EmptyFragment
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager

class EmptyFragment : EmptyFragment(), ChatNotificationManager.NotificationListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.showStatusBar()
        activity.addNotificationListener(this)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        bottomNavigation!!.setTapOnClickListener {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
        }

        bottomNavigation!!.setSwipeDownOnClickListener {
            findNavController().navigate(R.id.action_callFragment_to_endCallFragment)
        }

        return view
    }

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.TAP        -> {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
            true
        }
        TouchEvent.Type.SWIPE_DOWN -> {
            findNavController().navigate(R.id.action_callFragment_to_endCallFragment)
            true
        }
        else                       -> super.onTouch(event)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onShow() {
        root!!.alpha = 0.3f
    }

    override fun onExpanded() = Unit

    override fun onDismiss() {
        root!!.alpha = 1f
    }
}