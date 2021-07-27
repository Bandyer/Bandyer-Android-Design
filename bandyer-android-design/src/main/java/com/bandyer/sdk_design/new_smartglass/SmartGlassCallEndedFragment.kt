package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentCallEndedBinding
import com.google.android.material.textview.MaterialTextView

abstract class SmartGlassCallEndedFragment: SmartGlassBaseFragment() {

    private lateinit var binding: BandyerFragmentCallEndedBinding

    protected lateinit var root: View
    protected lateinit var title: MaterialTextView
    protected lateinit var subtitle: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallEndedBinding.inflate(inflater, container, false)
        root = binding.root
        title = binding.bandyerTitle
        subtitle = binding.bandyerSubtitle
        return root
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}