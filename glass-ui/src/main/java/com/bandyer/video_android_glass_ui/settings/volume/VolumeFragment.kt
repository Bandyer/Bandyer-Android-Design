package com.bandyer.video_android_glass_ui.settings.volume

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bandyer.video_android_glass_ui.utils.CallAudioManager
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.call.EmptyFragmentArgs
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

    private var callAudioManager: CallAudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(args.enableTilt) tiltListener = this
        callAudioManager = CallAudioManager(requireContext())
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
                if(GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()

                callAudioManager?.maxVolume?.apply { bandyerSlider.maxProgress = this }
                callAudioManager?.currentVolume?.apply { bandyerSlider.progress = this }
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
        callAudioManager?.setVolume(binding.bandyerSlider.progress)
        findNavController().popBackStack()
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.increaseProgress() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.decreaseProgress() }
}
