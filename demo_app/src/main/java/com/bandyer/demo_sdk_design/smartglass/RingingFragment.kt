package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassRingingFragment
import com.bandyer.sdk_design.new_smartglass.BandyerSmartGlassTouchEvent

class RingingFragment: SmartGlassRingingFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.hideStatusBar()

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

    override fun onSmartGlassTouchEvent(event: BandyerSmartGlassTouchEvent): Boolean =
        when (event.type) {
            BandyerSmartGlassTouchEvent.Type.TAP -> {
                findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
                true
            }
            BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN -> {
                requireActivity().finish()
                true
            }
            else -> super.onSmartGlassTouchEvent(event)
        }
}