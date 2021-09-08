package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentRingingBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.google.android.material.textview.MaterialTextView

/**
 * SmartGlassRingingFragment. A base class for the ringing
 *fragment.
 */
abstract class SmartGlassRingingFragment: SmartGlassBaseFragment() {

    private var binding: BandyerFragmentRingingBinding? = null

    protected var root: View? = null
    protected var title: MaterialTextView? = null
    protected var subtitle: MaterialTextView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentRingingBinding.inflate(inflater, container, false)
        root = binding!!.root
        title = binding!!.bandyerTitle
        subtitle = binding!!.bandyerSubtitle
        bottomActionBar = binding!!.bandyerBottomActionBar
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        title = null
        subtitle = null
        bottomActionBar = null
    }
}