package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentRingingBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils

/**
 * CallRingingFragment
 */
class CallRingingFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentRingingBinding? = null
    override val binding: BandyerGlassFragmentRingingBinding get() = _binding!!

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        // Add view binding
        _binding = BandyerGlassFragmentRingingBinding
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

    override fun onTap() = true.also { findNavController().navigate(R.id.action_ringingFragment_to_emptyFragment) }

    override fun onSwipeDown() = true.also { requireActivity().finish() }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}