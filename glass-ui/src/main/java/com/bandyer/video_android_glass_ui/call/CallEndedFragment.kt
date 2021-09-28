package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentCallEndedBinding
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallEndedFragment
 */
class CallEndedFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenDialogBinding get() = _binding!!

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

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
            inflater.cloneInContext(android.view.ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

        // Set OnClickListeners for realwear voice commands
        with(binding.bandyerBottomNavigation) {
            setTapOnClickListener { onTap() }
            setSwipeDownOnClickListener { onSwipeDown() }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        activity.removeNotificationListener(this)
    }

    override fun onTap() = true.also { requireActivity().finish() }

    override fun onSwipeDown() = true.also { requireActivity().finish() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}