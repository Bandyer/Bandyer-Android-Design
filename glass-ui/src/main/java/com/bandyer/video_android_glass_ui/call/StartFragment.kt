package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentStartBinding
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.*

/**
 * StartFragment, used as start destination in the nav graph
 */
class StartFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentStartBinding? = null
    override val binding: BandyerGlassFragmentStartBinding get() = _binding!!

    private val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

    private val args: StartFragmentArgs by lazy { StartFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

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
                callState
                    .takeWhile { it is Call.State.Connecting || it == Call.State.Disconnected }
                    .combine(participants) { state, participants ->
                        // TODO in caso di stato non previsto cosa mostrare
                        when {
                            state is Call.State.Connecting && participants.me == participants.creator ->
                                findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToDialingFragment(args.enableTilt, args.options))
                            state == Call.State.Disconnected && participants.me != participants.creator ->
                                findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToRingingFragment(args.enableTilt, args.options))
                            else -> Unit
                        }
                    }.launchIn(this@repeatOnStarted)
            }
        }

        return binding.root
    }

    override fun onTap(): Boolean = false

    override fun onSwipeDown(): Boolean = true.also {
        viewModel.hangUp()
        requireActivity().finish()
    }

    override fun onSwipeForward(isKeyEvent: Boolean): Boolean = false

    override fun onSwipeBackward(isKeyEvent: Boolean): Boolean = false
}