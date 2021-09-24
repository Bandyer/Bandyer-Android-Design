package com.bandyer.app_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.app_design.R
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent
import com.bandyer.video_android_glass_ui.chat.notification.BandyerNotificationManager

class RingingFragment: com.bandyer.video_android_glass_ui.call.BandyerGlassRingingFragment(), BandyerNotificationManager.NotificationListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.hideStatusBar()
        activity.addNotificationListener(this)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        bottomActionBar!!.setTapOnClickListener {
            findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        title!!.text = "Mario Draghi"
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onShow() {
        root!!.setAlphaWithAnimation(0f, 100L)
    }

    override fun onExpanded() = Unit

    override fun onDismiss() {
        root!!.setAlphaWithAnimation(1f, 100L)
    }

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean =
        when (event.type) {
            BandyerGlassTouchEvent.Type.TAP        -> {
                findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
                true
            }
            BandyerGlassTouchEvent.Type.SWIPE_DOWN -> {
                requireActivity().finish()
                true
            }
            else                                                                           -> super.onSmartGlassTouchEvent(event)
        }
}