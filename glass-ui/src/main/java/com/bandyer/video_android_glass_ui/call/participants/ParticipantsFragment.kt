package com.bandyer.video_android_glass_ui.call.participants

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.model.internal.UserState
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentParticipantsBinding
import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

/**
 * ParticipantsFragment
 */
internal class ParticipantsFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentParticipantsBinding? = null
    override val binding: BandyerGlassFragmentParticipantsBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<CallParticipantItem>? = null

    private var currentParticipantIndex = -1

    private val args: ParticipantsFragmentArgs by lazy { ParticipantsFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

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

                                val callUserDetails = viewModel.userDetails.value
                                val userDetails = callUserDetails.data.firstOrNull { it.userAlias == participant.userAlias }

                                when {
                                    userDetails?.avatarUrl != null -> setAvatar(userDetails.avatarUrl)
                                    userDetails?.avatarUri != null -> setAvatar(userDetails.avatarUri)
                                    userDetails?.avatarResId != null -> setAvatar(userDetails.avatarResId)
                                    else -> setAvatar(null)
                                }

                                val formattedText = userDetails?.let { callUserDetails.formatter?.participantFormat?.invoke(it) } ?: ""
                                setAvatarBackgroundAndLetter(formattedText)

                                repeatOnStarted {
                                    stateJob = participant.state.onEach {
                                        when(it) {
                                            is CallParticipant.State.Online.Invited -> setState(
                                                UserState.Invited(true))
                                            is CallParticipant.State.Online -> setState(UserState.Online)
                                            is CallParticipant.State.Offline.Invited -> setState(
                                                UserState.Invited(false))
                                            is CallParticipant.State.Offline-> setState(UserState.Offline, it.lastSeen)
                                        }
                                    }.launchIn(this)
                                }
                            }
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }

                repeatOnStarted {
                    viewModel.call.participants
                        .takeWhile { it.others.plus(it.me).isNotEmpty()  }
                        .collect { participants ->
                            val items = listOf(participants.me).plus(participants.others).map { CallParticipantItem(it, viewModel.userDetails) }
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


