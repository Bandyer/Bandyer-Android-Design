package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.postDelayed
import androidx.navigation.fragment.navArgs
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallEndedFragment
 */
internal class CallEndedFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    private val args: CallEndedFragmentArgs by navArgs()

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAndRemoveTask()
        }

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_callEndedStyle)
        _binding = BandyerGlassFragmentFullScreenLogoDialogBinding.inflate(
            inflater.cloneInContext(android.view.ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        ).apply {
            if(GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()
            bandyerTitle.text = args.title
            bandyerSubtitle.text = args.subtitle
        }

        return binding.root
    }

    override fun onServiceBound() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.postDelayed(AUTO_FINISH_TIME) { activity?.finishAndRemoveTask() }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = true.also { requireActivity().finishAndRemoveTask() }

    override fun onSwipeDown() = true.also { requireActivity().finishAndRemoveTask() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    private companion object {
        const val AUTO_FINISH_TIME = 3000L
    }
}