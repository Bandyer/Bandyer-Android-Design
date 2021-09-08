package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentCallBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BandyerBottomActionBarView

/**
 * SmartGlassCallFragment. A base class for the call fragment.
 */
abstract class SmartGlassCallFragment: SmartGlassBaseFragment(), BandyerSmartGlassTouchEventListener {

    private var binding: BandyerFragmentCallBinding? = null

    protected var root: View? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallBinding.inflate(inflater, container, false)
        root = binding!!.root
        bottomActionBar = binding!!.bandyerBottomActionBar
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        bottomActionBar = null
    }
}
