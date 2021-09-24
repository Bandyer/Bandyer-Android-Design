package com.bandyer.app_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.app_design.R
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_core_ui.extensions.ViewExtensions.setAlphaWithAnimation
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.call.participants.ParticipantsFragment
import com.bandyer.video_android_glass_ui.chat.notification.ChatNotificationManager
import com.bandyer.video_android_glass_ui.participants.ParticipantData
import com.bandyer.video_android_glass_ui.participants.ParticipantStateTextView
import com.bandyer.video_android_glass_ui.call.participants.CallParticipantItem
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import java.time.Instant
import java.time.temporal.ChronoUnit

class ParticipantsFragment : ParticipantsFragment(), TiltController.TiltListener, ChatNotificationManager.NotificationListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    private var currentParticipantIndex = -1

    private var participantsData: List<ParticipantData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tiltController =
                TiltController(
                    requireContext(),
                    this
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity.addNotificationListener(this)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        participantsData = listOf(
            ParticipantData(
                "Mario Rossi",
                "Mario Rossi",
                ParticipantData.UserState.ONLINE,
                R.drawable.sample_image,
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

        participantsData.forEach {
            itemAdapter!!.add(CallParticipantItem(it.userAlias))
        }

        rvParticipants!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rvParticipants!!.layoutManager
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                currentParticipantIndex = layoutManager!!.getPosition(foundView)
                val data = participantsData[currentParticipantIndex]
                avatar!!.setText(data.name.first().toString())
                avatar!!.setBackground(data.userAlias.parseToColor())
                when {
                    data.avatarImageId != null -> avatar!!.setImage(data.avatarImageId)
                    data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl!!)
                    else -> avatar!!.setImage(null)
                }
                if (data.avatarImageId != null) avatar!!.setImage(data.avatarImageId)
                else if (data.avatarImageUrl != null) avatar!!.setImage(data.avatarImageUrl!!)
                contactStateDot!!.isActivated =
                    data.userState == ParticipantData.UserState.ONLINE
                with(contactStateText!!) {
                    when (data.userState) {
                        ParticipantData.UserState.INVITED -> setContactState(
                            ParticipantStateTextView.State.INVITED
                        )
                        ParticipantData.UserState.OFFLINE -> setContactState(
                            ParticipantStateTextView.State.LAST_SEEN,
                            data.lastSeenTime
                        )
                        ParticipantData.UserState.ONLINE  -> setContactState(
                            ParticipantStateTextView.State.ONLINE
                        )
                    }
                }
            }
        })

        bottomActionBar!!.setSwipeOnClickListener {
            rvParticipants!!.horizontalSmoothScrollToNext(currentParticipantIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.removeNotificationListener(this)
    }

    override fun onShow() {
        root!!.setAlphaWithAnimation(0f, 100L)
    }

    override fun onExpanded() = Unit

    override fun onDismiss() {
        root!!.setAlphaWithAnimation(1f, 100L)
    }

    override fun onTilt(x: Float, y: Float, z: Float) = rvParticipants!!.scrollBy((x * resources.displayMetrics.densityDpi / 5).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.showStatusBarCenteredTitle()
        activity.setStatusBarColor(
            ResourcesCompat.getColor(
                resources,
                R.color.bandyer_glass_background_color,
                null
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onStop() {
        super.onStop()
        activity.hideStatusBarCenteredTitle()
        activity.setStatusBarColor(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
    }

    override fun onTouch(event: TouchEvent): Boolean = when (event.type) {
        TouchEvent.Type.SWIPE_FORWARD  -> {
            if(event.source == TouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.horizontalSmoothScrollToNext(currentParticipantIndex)
                true
            } else false
        }
        TouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == TouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.horizontalSmoothScrollToPrevious(currentParticipantIndex)
                true
            } else false
        }
        TouchEvent.Type.SWIPE_DOWN     -> {
            findNavController().popBackStack()
            true
        }
        else                           -> super.onTouch(event)
    }
}