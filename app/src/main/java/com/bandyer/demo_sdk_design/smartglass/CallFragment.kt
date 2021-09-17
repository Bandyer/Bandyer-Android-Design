package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.BandyerGlassTouchEvent

class CallFragment : com.bandyer.video_android_glass_ui.call.BandyerGlassCallFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()

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
}