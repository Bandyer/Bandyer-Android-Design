package com.bandyer.video_android_glass_ui.settings.zoom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentZoomBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener

/**
 * ZoomFragment
 */
internal class ZoomFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentZoomBinding? = null
    override val binding: BandyerGlassFragmentZoomBinding get() = _binding!!

    private var deltaAzimuth = 0f

    private val args: ZoomFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(args.enableTilt) tiltListener = this
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
        _binding = BandyerGlassFragmentZoomBinding
            .inflate(inflater, container, false)
            .apply { if(GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear() }

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

    override fun onTap() = true.also { findNavController().popBackStack() }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.increaseProgress(0.1f) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also { binding.bandyerSlider.decreaseProgress(0.1f) }
}
