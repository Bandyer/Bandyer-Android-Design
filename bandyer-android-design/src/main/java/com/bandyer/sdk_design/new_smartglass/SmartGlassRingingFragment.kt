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

    protected lateinit var root: View
    protected lateinit var title: MaterialTextView
    protected lateinit var subtitle: MaterialTextView
    protected lateinit var bottomActionBar: BottomActionBarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentRingingBinding.inflate(inflater, container, false)
        root = binding.root
        title = binding.bandyerTitle
        subtitle = binding.bandyerSubtitle
        bottomActionBar = binding.bottomActionBar
        return root
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}