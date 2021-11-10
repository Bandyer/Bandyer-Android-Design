package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.common.ConnectingFragment
import com.bandyer.video_android_glass_ui.safeNavigate
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * RingingFragment
 */
internal class RingingFragment : ConnectingFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_dialingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onConnected() { findNavController().safeNavigate(RingingFragmentDirections.actionRingingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean) { binding.bandyerSubtitle.text =  resources.getString(if(isGroupCall) R.string.bandyer_glass_ringing_group else R.string.bandyer_glass_ringing) }

    override fun onTap() = true.also { viewModel.answer() }

    override fun onSwipeDown() = true.also {
        viewModel.hangUp()
        requireActivity().finish()
    }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { binding.bandyerParticipants.smoothScrollBy(resources.displayMetrics.densityDpi / 2, 0) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { binding.bandyerParticipants.smoothScrollBy(-resources.displayMetrics.densityDpi / 2, 0) }
}