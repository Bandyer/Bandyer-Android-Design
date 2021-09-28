package com.bandyer.video_android_glass_ui.settings.zoom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.TiltFragment
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentZoomBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * ZoomFragment
 */
class ZoomFragment : TiltFragment() {

    //    private val activity by lazy { requireActivity() as SmartGlassActivity }

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
        //        activity.addNotificationListener(this)

        // Add view binding
        _binding = BandyerGlassFragmentZoomBinding.inflate(inflater, container, false)

        // Set OnClickListeners for realwear voice commands
        with(binding.bandyerBottomNavigation) {
            setTapOnClickListener { onTap() }
            setSwipeDownOnClickListener { onSwipeDown() }
        }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        activity.removeNotificationListener(this)
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
