package com.bandyer.video_android_glass_ui.call.participants

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.NavGraphViewModel
import com.bandyer.video_android_glass_ui.NavGraphViewModelFactory
import com.bandyer.video_android_glass_ui.common.UserState
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentParticipantsBinding
import com.bandyer.video_android_glass_ui.settings.volume.VolumeFragmentArgs
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

/**
 * ParticipantsFragment
 */
class ParticipantsFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentParticipantsBinding? = null
    override val binding: BandyerGlassFragmentParticipantsBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<CallParticipantItem>? = null

    private var currentParticipantIndex = -1

    private val args: VolumeFragmentArgs by navArgs()

    private val viewModel: NavGraphViewModel by navGraphViewModels(R.id.smartglass_nav_graph) { NavGraphViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(args.enableTilt) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Apply theme wrapper and add view binding
        _binding = BandyerGlassFragmentParticipantsBinding
            .inflate(inflater, container, false)
            .apply {
                if(GlassDeviceUtils.isRealWear)
                    bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                with(bandyerParticipants) {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false

                    addItemDecoration(HorizontalCenterItemDecoration())
                    addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        private var stateJob: Job? = null

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
                            currentParticipantIndex = layoutManager.getPosition(foundView)

                            stateJob?.cancel()

                            val participant = itemAdapter!!.getAdapterItem(currentParticipantIndex).participant
                            with(binding.bandyerUserInfo) {
                                hideName(true)
                                participant.avatarUrl?.apply { setAvatar(this) }
                                setAvatarBackgroundAndLetter(participant.username)

                                repeatOnStarted {
                                    stateJob = participant.state.onEach {
                                        setState(
                                            when(it) {
                                                is CallParticipant.State.Online.Invited -> UserState.Invited(true)
                                                is CallParticipant.State.Online -> UserState.Online
                                                is CallParticipant.State.Offline.Invited -> UserState.Invited(false)
                                                is CallParticipant.State.Offline-> UserState.Offline
                                            }
                                        )
                                    }.launchIn(this)
                                }
                            }
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }

                repeatOnStarted {
                    viewModel.participants
                        .takeWhile { it.others.plus(it.me).isNotEmpty()  }
                        .collect { participants ->
                            val items = participants.others.plus(participants.me).map { CallParticipantItem(it) }
                            FastAdapterDiffUtil[itemAdapter!!] = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                        }
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
        itemAdapter = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerParticipants.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) =
        (isKeyEvent && currentParticipantIndex != -1).also { if (it) binding.bandyerParticipants.horizontalSmoothScrollToNext(currentParticipantIndex) }

    override fun onSwipeBackward(isKeyEvent: Boolean) =
        (isKeyEvent && currentParticipantIndex != -1).also { if (it) binding.bandyerParticipants.horizontalSmoothScrollToPrevious(currentParticipantIndex) }
}


