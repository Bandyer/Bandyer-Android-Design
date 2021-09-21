package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent
import com.bandyer.video_android_glass_ui.chat.notification.BandyerNotificationManager

class CallEndedFragment : com.bandyer.video_android_glass_ui.call.BandyerGlassCallEndedFragment(), BandyerNotificationManager.NotificationListener {

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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(
            ResourcesCompat.getColor(
                resources,
                R.color.bandyer_glass_background_color,
                null
            )
        )
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
    }

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean = when (event.type) {
        BandyerGlassTouchEvent.Type.TAP, BandyerGlassTouchEvent.Type.SWIPE_DOWN -> {
            requireActivity().finish()
            true
        }
        else                                                                                                                                                    -> super.onSmartGlassTouchEvent(event)
    }

    override fun onShow() {
        root!!.setAlphaWithAnimation(0f, 100L)
    }

    override fun onExpanded() = Unit

    override fun onDismiss() {
        root!!.setAlphaWithAnimation(1f, 100L)
    }
}