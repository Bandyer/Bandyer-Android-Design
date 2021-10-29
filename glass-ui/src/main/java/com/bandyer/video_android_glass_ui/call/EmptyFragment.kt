package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.NavGraphViewModel
import com.bandyer.video_android_glass_ui.NavGraphViewModelFactory
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentEmptyBinding
import com.bandyer.video_android_glass_ui.safeNavigate
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils

/**
 * EmptyFragment
 */
class EmptyFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentEmptyBinding? = null
    override val binding: BandyerGlassFragmentEmptyBinding get() = _binding!!

    private val args: EmptyFragmentArgs by navArgs()

    private val viewModel: NavGraphViewModel by navGraphViewModels(R.id.smartglass_nav_graph) { NavGraphViewModelFactory }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.hangUp()
        }

        // Add view binding
        _binding = BandyerGlassFragmentEmptyBinding
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

    override fun onTap() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToMenuFragment(args.enableTilt, args.options)) }

    override fun onSwipeDown() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToEndCallFragment()) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}
