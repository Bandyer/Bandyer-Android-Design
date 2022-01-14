package com.bandyer.video_android_glass_ui.settings.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentVolumeBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener

/**
 * VolumeFragment
 */
internal class VolumeFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentVolumeBinding? = null
    override val binding: BandyerGlassFragmentVolumeBinding get() = _binding!!

    private var deltaAzimuth = 0f

    private val args: VolumeFragmentArgs by lazy { VolumeFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private val viewModel: GlassViewModel by viewModels { GlassViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Add view binding
        _binding = BandyerGlassFragmentVolumeBinding
            .inflate(inflater, container, false)
            .apply {
                if (GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()

                with(bandyerSlider) {
                    val volume = viewModel.volume
                    maxProgress = volume.max
                    progress = volume.current
                }

                root.setOnTouchListener { _, _ -> true }
            }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) {
        this.deltaAzimuth += deltaAzimuth
        if (this.deltaAzimuth >= 2) onSwipeForward(true).also { this.deltaAzimuth = 0f }
        else if (this.deltaAzimuth <= -2) onSwipeBackward(true).also { this.deltaAzimuth = 0f }
    }

    override fun onTap() = true.also {
        viewModel.setVolume(binding.bandyerSlider.progress)
        findNavController().popBackStack()
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.increaseProgress() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.decreaseProgress() }
}