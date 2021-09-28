package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentEmptyBinding

/**
 * EmptyFragment
 */
class EmptyFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentEmptyBinding? = null
    override val binding: BandyerGlassFragmentEmptyBinding get() = _binding!!

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.showStatusBar()
//        activity.addNotificationListener(this)

        // Add view binding
        _binding = BandyerGlassFragmentEmptyBinding.inflate(inflater, container, false)

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

    override fun onTap() = true.also { findNavController().navigate(R.id.action_emptyFragment_to_menuFragment) }

    override fun onSwipeDown() = true.also { findNavController().navigate(R.id.action_emptyFragment_to_endCallFragment) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}
