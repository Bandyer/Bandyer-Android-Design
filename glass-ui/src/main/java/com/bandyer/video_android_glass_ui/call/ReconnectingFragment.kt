package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.safeNavigate
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * ConnectingFragment
 */
internal class ReconnectingFragment : ConnectingFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_reconnectingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onConnected() { findNavController().safeNavigate(ReconnectingFragmentDirections.actionReconnectingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean) { binding.bandyerSubtitle.text = resources.getString(R.string.bandyer_glass_connecting) }

    override fun onTap() = false

    override fun onSwipeDown() = true.also { viewModel.hangUp() }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}