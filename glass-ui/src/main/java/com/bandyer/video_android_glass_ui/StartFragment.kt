package com.bandyer.video_android_glass_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.*

/**
 * An empty fragment used as start destination in the nav graph
 */
class StartFragment : Fragment() {

    private val viewModel: NavGraphViewModel by navGraphViewModels(R.id.smartglass_nav_graph) { NavGraphViewModelFactory }

    private val args: StartFragmentArgs by lazy { StartFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.hangUp()
        }

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

        return inflater.inflate(R.layout.bandyer_glass_fragment_start, container, false)
    }
}