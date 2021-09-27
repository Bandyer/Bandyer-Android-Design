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
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallEndedFragment
 */
class CallEndedFragment : BaseFragment(), ChatNotificationManager.NotificationListener {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    private val binding get() = _binding!!

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.showStatusBar()
//        activity.addNotificationListener(this)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_callEndedStyle)
        _binding = BandyerGlassFragmentFullScreenDialogBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

        // Set OnClickListeners for realwear voice commands
        binding.bandyerBottomNavigation.setTapOnClickListener {
            findNavController().navigate(R.id.action_callFragment_to_menuFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        activity.removeNotificationListener(this)
    }

    override fun onResume() {
        super.onResume()
//        activity.setStatusBarColor(
//            ResourcesCompat.getColor(
//                resources,
//                R.color.bandyer_glass_background_color,
//                null
//            )
//        )
    }

    override fun onStop() {
        super.onStop()
//        activity.setStatusBarColor(null)
    }

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.TAP, TouchEvent.Type.SWIPE_DOWN     -> {
            requireActivity().finish()
            true
        }
        else                                                -> super.onTouch(event)
    }

    override fun onShow() = binding.root.setAlphaWithAnimation(0f, 100L)

    override fun onExpanded() = Unit

    override fun onDismiss() = binding.root.setAlphaWithAnimation(1f, 100L)

}