package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * EndCallFragment
 */
class EndCallFragment : BaseFragment(), ChatNotificationManager.NotificationListener {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    private val binding get() = _binding!!

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.hideStatusBar()
//        activity.addNotificationListener(this)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_endCallStyle)
        _binding = BandyerGlassFragmentFullScreenDialogBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

        with(binding.bandyerBottomNavigation) {
            // Set OnClickListeners for realwear voice commands
            setTapOnClickListener {
                findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
            }

            setSwipeDownOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        activity.removeNotificationListener(this)
        _binding = null
    }

    override fun onShow() = binding.root.setAlphaWithAnimation(0f, 100L)

    override fun onExpanded() = Unit

    override fun onDismiss() = binding.root.setAlphaWithAnimation(1f, 100L)

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.TAP         -> {
            findNavController().navigate(R.id.action_endCallFragment_to_callEndedFragment)
            true
        }
        TouchEvent.Type.SWIPE_DOWN  -> {
            findNavController().popBackStack()
            true
        }
        else                        -> super.onTouch(event)
    }
}