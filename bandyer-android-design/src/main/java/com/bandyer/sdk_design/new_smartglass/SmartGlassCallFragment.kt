package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bandyer.sdk_design.databinding.BandyerFragmentCallBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView

abstract class SmartGlassCallFragment: Fragment(), SmartGlassTouchEventListener {

    private lateinit var binding: BandyerFragmentCallBinding

    protected lateinit var root: View
    protected lateinit var bottomActionBar: BottomActionBarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallBinding.inflate(inflater, container, false)
        root = binding.root
        bottomActionBar = binding.bottomActionBar
        return root
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}