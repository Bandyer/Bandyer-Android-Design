package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent

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

    override fun onSmartGlassTouchEvent(event: BandyerGlassTouchEvent): Boolean = when (event.type) {
        BandyerGlassTouchEvent.Type.TAP        -> {
            findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
            true
        }
        BandyerGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else                                                                           -> super.onSmartGlassTouchEvent(event)
    }
}