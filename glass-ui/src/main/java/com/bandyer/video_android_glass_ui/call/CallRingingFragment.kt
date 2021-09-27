package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentRingingBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallRingingFragment
 */
class CallRingingFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentRingingBinding? = null
    override val binding: BandyerGlassFragmentRingingBinding get() = _binding!!

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

        // Set OnClickListeners for realwear voice commands
        with(binding.bandyerBottomNavigation) {
            setTapOnClickListener { onTap() }
            setSwipeDownOnClickListener { onSwipeDown() }
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

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.TAP        -> onTap()
            TouchEvent.Type.SWIPE_DOWN -> onSwipeDown()
            else                       -> super.onTouch(event)
        }

    private fun onTap(): Boolean {
        findNavController().navigate(R.id.action_ringingFragment_to_emptyFragment)
        return true
    }

    private fun onSwipeDown(): Boolean {
        requireActivity().finish()
        return true
    }
}