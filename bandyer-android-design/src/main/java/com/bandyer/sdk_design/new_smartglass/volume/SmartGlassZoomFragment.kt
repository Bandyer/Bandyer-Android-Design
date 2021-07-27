package com.bandyer.sdk_design.new_smartglass.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentZoomBinding
import com.bandyer.sdk_design.new_smartglass.BandyerSlider
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView

abstract class SmartGlassZoomFragment : SmartGlassBaseFragment() {

    protected lateinit var binding: BandyerFragmentZoomBinding
    protected lateinit var root: View
    protected lateinit var slider: BandyerSlider
    protected lateinit var bottomActionBar: BottomActionBarView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentZoomBinding.inflate(inflater, container, false)
        root = binding.root
        slider = binding.slider
        bottomActionBar = binding.bottomActionBar
        root.setOnTouchListener { _, event -> binding.slider.onTouchEvent(event) }
        return root
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}
