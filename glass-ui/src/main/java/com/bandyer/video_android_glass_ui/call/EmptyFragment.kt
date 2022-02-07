package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentEmptyBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.safeNavigate

/**
 * EmptyFragment
 */
internal class EmptyFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentEmptyBinding? = null
    override val binding: BandyerGlassFragmentEmptyBinding get() = _binding!!

    private val viewModel: GlassViewModel by activityViewModels {
        GlassViewModelFactory.getInstance(
            GlassUIProvider.callService!!.get() as CallUIDelegate,
            GlassUIProvider.callService!!.get() as DeviceStatusDelegate,
            GlassUIProvider.callService!!.get() as CallUIController
        )
    }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onHangup()
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

    override fun onTap() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToMenuFragment()) }

    override fun onSwipeDown() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToEndCallFragment()) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}
