package com.bandyer.sdk_design.new_smartglass.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.databinding.BandyerFragmentVolumeBinding
import com.bandyer.sdk_design.new_smartglass.BandyerSlider
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView

abstract class SmartGlassVolumeFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentVolumeBinding? = null

    protected var root: View? = null
    protected var slider: BandyerSlider? = null
    protected var bottomActionBar: BottomActionBarView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentVolumeBinding.inflate(inflater, container, false)
        root = binding!!.root
        slider = binding!!.slider
        bottomActionBar = binding!!.bottomActionBar
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        slider = null
        bottomActionBar = null
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            slider!!.incrementProgress()
            true
        }
        SmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            slider!!.decrementProgress()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}
