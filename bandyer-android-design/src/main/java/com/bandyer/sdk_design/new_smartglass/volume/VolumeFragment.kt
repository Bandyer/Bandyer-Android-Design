package com.bandyer.sdk_design.new_smartglass.volume

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import com.bandyer.sdk_design.databinding.BandyerFragmentVolumeBinding
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEventListener

class VolumeFragment : Fragment(), SmartGlassTouchEventListener {

    private lateinit var binding: BandyerFragmentVolumeBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentVolumeBinding.inflate(inflater, container, false)
        val root = binding.root
        root.setOnTouchListener { _, event -> binding.slider.onTouchEvent(event) }
        return root
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false

}
