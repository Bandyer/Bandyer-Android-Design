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
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentParticipantsBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.*

/**
 * ParticipantsFragment
 */
internal class ParticipantsFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentParticipantsBinding? = null
    override val binding: BandyerGlassFragmentParticipantsBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<CallParticipantItem>? = null

    private var currentParticipantIndex = -1

    private val args: ParticipantsFragmentArgs by lazy { ParticipantsFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private val viewModel: GlassViewModel by activityViewModels {
        GlassViewModelFactory.getInstance(
            GlassUIProvider.callService!!.get() as CallUIDelegate,
            GlassUIProvider.callService!!.get() as DeviceStatusDelegate,
            GlassUIProvider.callService!!.get() as CallUIController
        )
    }

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
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
                            currentParticipantIndex = layoutManager.getPosition(foundView)

                            val participant = itemAdapter!!.getAdapterItem(currentParticipantIndex).participant
                            with(binding.bandyerUserInfo) {
                                hideName(true)

                                val userDetailsWrapper = viewModel.userDetailsWrapper.value
                                val userDetails = userDetailsWrapper.data.firstOrNull { it.userAlias == participant.userAlias } ?: UserDetails(participant.userAlias)

                                when {
                                    userDetails.avatarUrl != null -> setAvatar(userDetails.avatarUrl)
                                    userDetails.avatarUri != null -> setAvatar(userDetails.avatarUri)
                                    userDetails.avatarResId != null -> setAvatar(userDetails.avatarResId)
                                    else -> setAvatar(null)
                                }

                                val formattedText = userDetailsWrapper.formatters.callFormatter.format(userDetails)
                                setAvatarBackgroundAndLetter(formattedText)
                            }
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }

                repeatOnStarted {
                    with(viewModel) {
                        combine(inCallParticipants, participants) { inCallParticipants, participants -> Pair(inCallParticipants, participants) }
                            .takeWhile { it.first.isNotEmpty()  }
                            .collect { pair ->
                                val sortedList = pair.first.sortedBy { pair.second.me.userAlias != it.userAlias }
                                val items = sortedList.map { CallParticipantItem(it, viewModel.userDetailsWrapper) }
                                FastAdapterDiffUtil[itemAdapter!!] = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                            }
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


