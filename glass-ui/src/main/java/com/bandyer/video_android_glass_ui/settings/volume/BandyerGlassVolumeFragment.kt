package com.bandyer.video_android_glass_ui.settings.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentVolumeBinding
import com.bandyer.video_android_glass_ui.BandyerGlassBaseFragment
import com.bandyer.video_android_glass_ui.bottom_action_bar.BandyerBottomActionBarView
import com.bandyer.video_android_glass_ui.settings.BandyerSlider

/**
 * BandyerGlassVolumeFragment. A base class for the volume fragment.
 */
abstract class BandyerGlassVolumeFragment : BandyerGlassBaseFragment() {

    private var binding: BandyerFragmentVolumeBinding? = null

    protected var root: View? = null
    protected var slider: BandyerSlider? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    /**
     * @suppress
     */
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

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        slider = null
        bottomActionBar = null
    }
}
