package com.bandyer.video_android_glass_ui.settings.zoom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.TiltFragment
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentZoomBinding

/**
 * ZoomFragment
 */
class ZoomFragment : TiltFragment() {

    private var _binding: BandyerGlassFragmentZoomBinding? = null
    override val binding: BandyerGlassFragmentZoomBinding get() = _binding!!

    private var deltaAzimuth = 0f

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
        _binding = BandyerGlassFragmentZoomBinding
            .inflate(inflater, container, false)
            .apply { bandyerBottomNavigation.setListenersForRealwear() }

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
        if (deltaAzimuth >= 2) onSwipeForward(true).also { this.deltaAzimuth = 0f }
        else if (deltaAzimuth <= -2) onSwipeBackward(true).also { this.deltaAzimuth = 0f }
    }

    override fun onTap() = true.also { findNavController().popBackStack() }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.increaseProgress(0.1f) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.decreaseProgress(0.1f) }
}
