package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent
import com.bandyer.video_android_glass_ui.call.BandyerGlassCallFragment
import com.bandyer.video_android_glass_ui.chat.notification.BandyerNotificationManager

class CallFragment : BandyerGlassCallFragment(), BandyerNotificationManager.NotificationListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.showStatusBar()
        activity.addNotificationListener(this)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        bottomActionBar!!.setTapOnClickListener {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().navigate(R.id.action_callFragment_to_endCallFragment)
        }

        return view
    }

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean = when (event.type) {
        BandyerGlassTouchEvent.Type.TAP                                           -> {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
            true
        }
        BandyerGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().navigate(R.id.action_callFragment_to_endCallFragment)
            true
        }
        else                                                                           -> super.onSmartGlassTouchEvent(event)
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