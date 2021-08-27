package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassCallFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent

class CallFragment : SmartGlassCallFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.TAP -> {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
            true
        }
        SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().navigate(R.id.action_callFragment_to_endCallFragment)
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}