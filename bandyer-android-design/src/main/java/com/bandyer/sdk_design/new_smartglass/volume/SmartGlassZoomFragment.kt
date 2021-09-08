package com.bandyer.sdk_design.new_smartglass.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentZoomBinding
import com.bandyer.sdk_design.new_smartglass.BandyerSlider
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView

/**
 * SmartGlassZoomFragment. A base class for the zoom fragment.
 */
abstract class SmartGlassZoomFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentZoomBinding? = null

    protected var root: View? = null
    protected var slider: BandyerSlider? = null
    protected var bottomActionBar: BottomActionBarView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentZoomBinding.inflate(inflater, container, false)
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
}
