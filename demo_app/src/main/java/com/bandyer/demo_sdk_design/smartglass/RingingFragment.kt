package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassRingingFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class RingingFragment: SmartGlassRingingFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).hideStatusBar()
        val view = super.onCreateView(inflater, container, savedInstanceState)
        title!!.text = "Mario Draghi"
        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean =
        when (event) {
            SmartGlassTouchEvent.Event.TAP -> {
                findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
                true
            }
            SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
                requireActivity().finish()
                true
            }
            else -> false
        }
}