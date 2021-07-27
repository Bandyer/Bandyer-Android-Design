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

    protected var root: View? = null
    protected var bottomActionBar: BottomActionBarView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallBinding.inflate(inflater, container, false)
        root = binding.root
        bottomActionBar = binding.bottomActionBar
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root = null
        bottomActionBar = null
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}