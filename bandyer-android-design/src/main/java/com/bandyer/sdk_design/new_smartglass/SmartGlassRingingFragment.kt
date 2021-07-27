package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentRingingBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.google.android.material.textview.MaterialTextView

abstract class SmartGlassRingingFragment: SmartGlassBaseFragment() {

    private lateinit var binding: BandyerFragmentRingingBinding

    protected var root: View? = null
    protected var title: MaterialTextView? = null
    protected var subtitle: MaterialTextView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentRingingBinding.inflate(inflater, container, false)
        root = binding.root
        title = binding.bandyerTitle
        subtitle = binding.bandyerSubtitle
        bottomActionBar = binding.bottomActionBar
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root = null
        title = null
        subtitle = null
        bottomActionBar = null
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}