package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.BandyerFragmentCallEndedBinding
import com.google.android.material.textview.MaterialTextView

abstract class SmartGlassCallEndedFragment: SmartGlassBaseFragment() {

    private var binding: BandyerFragmentCallEndedBinding? = null

    protected var root: View? = null
    protected var title: MaterialTextView? = null
    protected var subtitle: MaterialTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallEndedBinding.inflate(inflater, container, false)
        root = binding!!.root
        title = binding!!.bandyerTitle
        subtitle = binding!!.bandyerSubtitle
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        title = null
        subtitle = null
    }
}