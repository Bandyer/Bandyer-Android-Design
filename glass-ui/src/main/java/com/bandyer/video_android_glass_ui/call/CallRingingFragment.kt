package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentRingingBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallRingingFragment
 */
abstract class CallRingingFragment: BaseFragment(), ChatNotificationManager.NotificationListener {

    private var _binding: BandyerGlassFragmentRingingBinding? = null
    private val binding get() = _binding!!

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.hideStatusBar()
//        activity.addNotificationListener(this)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_ringingStyle)
        _binding = BandyerGlassFragmentRingingBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

        with(binding.bandyerBottomNavigation) {
            // Set OnClickListeners for realwear voice commands
            setTapOnClickListener {
                findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
            }

            setSwipeDownOnClickListener {
                findNavController().popBackStack()
            }
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
    override fun onShow() = binding.root.setAlphaWithAnimation(0f, 100L)

    override fun onExpanded() = Unit

    override fun onDismiss() = binding.root.setAlphaWithAnimation(1f, 100L)

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.TAP        -> {
                findNavController().navigate(R.id.action_ringingFragment_to_callFragment)
                true
            }
            TouchEvent.Type.SWIPE_DOWN -> {
                requireActivity().finish()
                true
            }
            else                       -> super.onTouch(event)
        }
}