package com.bandyer.video_android_glass_ui.call.participants

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.GlassActivity
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TiltFragment
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentParticipantsBinding
import com.bandyer.video_android_glass_ui.participants.ParticipantData
import com.bandyer.video_android_glass_ui.participants.ParticipantStateTextView
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * ParticipantsFragment
 */
class ParticipantsFragment : TiltFragment() {

    private var _binding: BandyerGlassFragmentParticipantsBinding? = null
    override val binding: BandyerGlassFragmentParticipantsBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<CallParticipantItem>? = null

    private var currentParticipantIndex = -1

    private var participantsData: List<ParticipantData> = listOf(
        ParticipantData(
            "Mario Rossi",
            "Mario Rossi",
            ParticipantData.UserState.ONLINE,
            null,
            null,
            Instant.now().toEpochMilli()
        ),
        ParticipantData(
            "Felice Trapasso",
            "Felice Trapasso",
            ParticipantData.UserState.OFFLINE,
            null,
            "https://i.imgur.com/9I2qAlW.jpeg",
            Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
        ),
        ParticipantData(
            "Francesco Sala",
            "Francesco Sala",
            ParticipantData.UserState.INVITED,
            null,
            null,
            Instant.now().toEpochMilli()
        )
    )

    override fun onResume() {
        super.onResume()
        activity.showStatusBarCenteredTitle()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_glass_background_color, null))
    }

    override fun onStop() {
        super.onStop()
        activity.hideStatusBarCenteredTitle()
        activity.setStatusBarColor(null)
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
                bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                with(bandyerParticipants) {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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

                            val data = participantsData[currentParticipantIndex]

                            with(bandyerAvatar) {
                                setText(data.name.first().toString())
                                setBackground(data.userAlias.parseToColor())

                                when {
                                    data.avatarImageId != null -> setImage(data.avatarImageId)
                                    data.avatarImageUrl != null -> setImage(data.avatarImageUrl)
                                    else -> setImage(null)
                                }
                            }

                            bandyerContactStateDot.isActivated =
                                data.userState == ParticipantData.UserState.ONLINE

                            with(bandyerContactStateText) {
                                when (data.userState) {
                                    ParticipantData.UserState.INVITED -> setContactState(
                                        ParticipantStateTextView.State.INVITED
                                    )
                                    ParticipantData.UserState.OFFLINE -> setContactState(
                                        ParticipantStateTextView.State.LAST_SEEN,
                                        data.lastSeenTime
                                    )
                                    ParticipantData.UserState.ONLINE -> setContactState(
                                        ParticipantStateTextView.State.ONLINE
                                    )
                                }
                            }
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }
            }

        participantsData.forEach {
            itemAdapter!!.add(CallParticipantItem(it.userAlias))
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
        (isKeyEvent && currentParticipantIndex != -1).also {
            if (it) binding.bandyerParticipants.horizontalSmoothScrollToNext(
                currentParticipantIndex
            )
        }

    override fun onSwipeBackward(isKeyEvent: Boolean) =
        (isKeyEvent && currentParticipantIndex != -1).also {
            if (it) binding.bandyerParticipants.horizontalSmoothScrollToPrevious(
                currentParticipantIndex
            )
        }
}


