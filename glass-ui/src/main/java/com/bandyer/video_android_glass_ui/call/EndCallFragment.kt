package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * EndCallFragment
 */
internal class EndCallFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenDialogBinding get() = _binding!!

    private val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_endCallStyle)
        _binding = BandyerGlassFragmentFullScreenDialogBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            )
            .apply { if (GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear() }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = true.also { viewModel.hangUp() }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}