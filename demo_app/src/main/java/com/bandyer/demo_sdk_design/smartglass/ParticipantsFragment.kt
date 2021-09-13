package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.demo_sdk_design.R
import com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent
import com.bandyer.video_android_glass_ui.contact.BandyerContactData
import com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView
import com.bandyer.video_android_glass_ui.contact.call_participant.BandyerCallParticipantItem
import com.bandyer.video_android_glass_ui.contact.call_participant.SmartGlassCallParticipantsFragment
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import com.bandyer.sdk_design.new_smartglass.smoothScrollToPrevious
import java.time.Instant
import java.time.temporal.ChronoUnit

class ParticipantsFragment : com.bandyer.video_android_glass_ui.contact.call_participant.SmartGlassCallParticipantsFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    private var currentParticipantIndex = -1

    private var participantsData: List<com.bandyer.video_android_glass_ui.contact.BandyerContactData> = listOf()

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
        val view = super.onCreateView(inflater, container, savedInstanceState)

        participantsData = listOf(
            com.bandyer.video_android_glass_ui.contact.BandyerContactData(
                "Mario Rossi",
                "Mario Rossi",
                com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.ONLINE,
                R.drawable.sample_image,
                null,
                Instant.now().toEpochMilli()
            ),
            com.bandyer.video_android_glass_ui.contact.BandyerContactData(
                "Felice Trapasso",
                "Felice Trapasso",
                com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.OFFLINE,
                null,
                "https://i.imgur.com/9I2qAlW.jpeg",
                Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
            ),
            com.bandyer.video_android_glass_ui.contact.BandyerContactData(
                "Francesco Sala",
                "Francesco Sala",
                com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.INVITED,
                null,
                null,
                Instant.now().toEpochMilli()
            )
        )

        participantsData.forEach {
            itemAdapter!!.add(com.bandyer.video_android_glass_ui.contact.call_participant.BandyerCallParticipantItem(it.userAlias))
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
                    data.userState == com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.ONLINE
                with(contactStateText!!) {
                    when (data.userState) {
                        com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.INVITED -> setContactState(
                            com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.INVITED
                        )
                        com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.OFFLINE -> setContactState(
                            com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.LAST_SEEN,
                            data.lastSeenTime
                        )
                        com.bandyer.video_android_glass_ui.contact.BandyerContactData.UserState.ONLINE  -> setContactState(
                            com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView.State.ONLINE
                        )
                    }
                }
            }
        })

        bottomActionBar!!.setSwipeOnClickListener {
            rvParticipants!!.smoothScrollToNext(currentParticipantIndex)
        }

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onTilt(x: Float, y: Float) = rvParticipants!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.showStatusBarCenteredTitle()
        activity.setStatusBarColor(
            ResourcesCompat.getColor(
                resources,
                R.color.bandyer_smartglass_background_color,
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

    override fun onSmartGlassTouchEvent(event: com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent): Boolean = when (event.type) {
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_FORWARD  -> {
            if(event.source == com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.smoothScrollToNext(currentParticipantIndex)
                true
            } else false
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.smoothScrollToPrevious(currentParticipantIndex)
                true
            } else false
        }
        com.bandyer.video_android_glass_ui.BandyerSmartGlassTouchEvent.Type.SWIPE_DOWN     -> {
            findNavController().popBackStack()
            true
        }
        else                                                                               -> super.onSmartGlassTouchEvent(event)
    }
}