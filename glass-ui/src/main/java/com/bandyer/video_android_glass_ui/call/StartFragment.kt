package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentStartBinding
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.safeNavigate
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.takeWhile

/**
 * StartFragment, used as start destination in the nav graph
 */
internal class StartFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentStartBinding? = null
    override val binding: BandyerGlassFragmentStartBinding get() = _binding!!

    private val viewModel: GlassViewModel by activityViewModels()

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onHangup()
        }

        _binding = BandyerGlassFragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onServiceConnected() {
        repeatOnStarted {
            viewModel.callState
                .takeWhile { it is Call.State.Connecting || it == Call.State.Disconnected }
                .combine(viewModel.participants) { state, participants ->
                    when {
                        state is Call.State.Connecting && participants.me == participants.creator() ->
                            findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToDialingFragment())
                        state == Call.State.Disconnected && participants.me != participants.creator() ->
                            findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToRingingFragment())
                        else -> Unit
                    }
                }.launchIn(this@repeatOnStarted)
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap(): Boolean = false

    override fun onSwipeDown(): Boolean = true.also {
        viewModel.onHangup()
        requireActivity().finishAndRemoveTask()
    }

    override fun onSwipeForward(isKeyEvent: Boolean): Boolean = false

    override fun onSwipeBackward(isKeyEvent: Boolean): Boolean = false
}