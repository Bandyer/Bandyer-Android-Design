package com.bandyer.video_android_glass_ui.settings.zoom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentZoomBinding
import com.bandyer.video_android_glass_ui.utils.TiltController
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * ZoomFragment
 */
class ZoomFragment : BaseFragment(), TiltController.TiltListener {

    //    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var _binding: BandyerGlassFragmentZoomBinding? = null
    override val binding: BandyerGlassFragmentZoomBinding get() = _binding!!

    private var deltaAzimuth = 0f

    private var tiltController: TiltController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tiltController = TiltController(requireContext(), this)
    }

    override fun onResume() {
        super.onResume()
        tiltController!!.requestAllSensors()
    }

    override fun onPause() {
        super.onPause()
        tiltController!!.releaseAllSensors()
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //        activity.addNotificationListener(this)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_zoomStyle)
        _binding = BandyerGlassFragmentZoomBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

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

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.TAP             -> onTap()
        TouchEvent.Type.SWIPE_DOWN      -> onSwipeDown()
        TouchEvent.Type.SWIPE_FORWARD   -> onSwipeForward()
        TouchEvent.Type.SWIPE_BACKWARD  -> onSwipeBackward()
        else -> super.onTouch(event)
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) {
        this.deltaAzimuth += deltaAzimuth
        if (deltaAzimuth >= 2) onSwipeForward().also { this.deltaAzimuth = 0f }
        else if (deltaAzimuth <= -2) onSwipeBackward().also { this.deltaAzimuth = 0f }
    }

    private fun onTap(): Boolean {
        findNavController().popBackStack()
        return true
    }

    private fun onSwipeDown(): Boolean {
        findNavController().popBackStack()
        return true
    }

    private fun onSwipeForward(): Boolean {
        binding.bandyerSlider.increaseProgress(0.1f)
        return true
    }

    private fun onSwipeBackward(): Boolean {
        binding.bandyerSlider.decreaseProgress(0.1f)
        return true
    }
}
