package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.call.SmartGlassEndCallFragment
import com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent

class EndCallFragment : com.bandyer.video_android_glass_ui.call.SmartGlassEndCallFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.hideStatusBar()

        val view = super.onCreateView(inflater, container, savedInstanceState)

        bottomActionBar!!.setTapOnClickListener {
            findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onSmartGlassTouchEvent(event: com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent): Boolean = when (event.type) {
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.TAP        -> {
            findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
            true
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else                                                                           -> super.onSmartGlassTouchEvent(event)
    }
}