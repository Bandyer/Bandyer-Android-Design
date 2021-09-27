package com.bandyer.video_android_glass_ui.settings.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentVolumeBinding
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.bandyer.video_android_glass_ui.common.SettingSlider

/**
 * VolumeFragment. A base class for the volume fragment.
 */
abstract class VolumeFragment : BaseFragment() {

    private var binding: BandyerFragmentVolumeBinding? = null

    protected var root: View? = null
    protected var slider: SettingSlider? = null
    protected var bottomNavigation: BottomNavigationView? = null

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
        bottomNavigation = binding!!.bandyerBottomActionBar
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
        bottomNavigation = null
    }
}
