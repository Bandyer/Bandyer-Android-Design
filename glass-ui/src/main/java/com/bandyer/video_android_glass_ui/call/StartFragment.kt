package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentStartBinding
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.safeNavigate
import kotlinx.coroutines.flow.*

/**
 * StartFragment, used as start destination in the nav graph
 */
internal class StartFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentStartBinding? = null
    override val binding: BandyerGlassFragmentStartBinding get() = _binding!!

    private val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.hangUp()
        }

        _binding = BandyerGlassFragmentStartBinding.inflate(inflater, container, false)

        repeatOnStarted {
            with(viewModel) {
                call.state
                    .takeWhile { it is Call.State.Connecting || it == Call.State.Disconnected }
                    .combine(call.participants) { state, participants ->
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

        return binding.root
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
        viewModel.hangUp()
        requireActivity().finish()
    }

    override fun onSwipeForward(isKeyEvent: Boolean): Boolean = false

    override fun onSwipeBackward(isKeyEvent: Boolean): Boolean = false
}