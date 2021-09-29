package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallEndedFragment
 */
class CallEndedFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenDialogBinding get() = _binding!!

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
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_callEndedStyle)
        _binding = BandyerGlassFragmentFullScreenDialogBinding.inflate(
            inflater.cloneInContext(android.view.ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        ).apply { if(GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear() }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = true.also { requireActivity().finish() }

    override fun onSwipeDown() = true.also { requireActivity().finish() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}