package com.bandyer.sdk_design.new_smartglass.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentVolumeBinding
import com.bandyer.sdk_design.new_smartglass.BandyerSlider
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BandyerBottomActionBarView

/**
 * SmartGlassVolumeFragment. A base class for the volume fragment.
 */
abstract class SmartGlassVolumeFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentVolumeBinding? = null

    protected var root: View? = null
    protected var slider: BandyerSlider? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentVolumeBinding.inflate(inflater, container, false)
        root = binding!!.root
        slider = binding!!.bandyerSlider
        bottomActionBar = binding!!.bandyerBottomActionBar
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
