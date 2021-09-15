package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.call.SmartGlassCallEndedFragment
import com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent

class CallEndedFragment : com.bandyer.video_android_glass_ui.call.SmartGlassCallEndedFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

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

        return view
    }

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(
            ResourcesCompat.getColor(
                resources,
                R.color.bandyer_smartglass_background_color,
                null
            )
        )
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
    }

    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent): Boolean = when (event.type) {
        BandyerSmartGlassTouchEvent.Type.TAP, BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            requireActivity().finish()
            true
        }
        else                                                                                                                                                    -> super.onSmartGlassTouchEvent(event)
    }
}