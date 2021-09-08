package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class ParticipantsFragment : SmartGlassParticipantsFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    private var currentParticipantIndex = -1

    private var participantsData: List<SmartGlassParticipantData> = listOf()

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

        if (Build.MODEL == resources.getString(R.string.bandyer_smartglass_realwear_model_name))
            bottomActionBar!!.setSwipeText(resources.getString(R.string.bandyer_smartglass_right_left))

        participantsData = listOf(
            SmartGlassParticipantData(
                "Mario Rossi",
                "Mario Rossi",
                SmartGlassParticipantData.UserState.ONLINE,
                R.drawable.sample_image,
                null,
                Instant.now().toEpochMilli()
            ),
            SmartGlassParticipantData(
                "Felice Trapasso",
                "Felice Trapasso",
                SmartGlassParticipantData.UserState.OFFLINE,
                null,
                "https://i.imgur.com/9I2qAlW.jpeg",
                Instant.now().minus(8, ChronoUnit.DAYS).toEpochMilli()
            ),
            SmartGlassParticipantData(
                "Francesco Sala",
                "Francesco Sala",
                SmartGlassParticipantData.UserState.INVITED,
                null,
                null,
                Instant.now().toEpochMilli()
            )
        )

        participantsData.forEach {
            itemAdapter!!.add(ParticipantItem(it.userAlias))
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
                    data.userState == SmartGlassParticipantData.UserState.ONLINE
                with(contactStateText!!) {
                    when (data.userState) {
                        SmartGlassParticipantData.UserState.INVITED -> setContactState(
                            ContactStateTextView.State.INVITED
                        )
                        SmartGlassParticipantData.UserState.OFFLINE -> setContactState(
                            ContactStateTextView.State.LAST_SEEN,
                            data.lastSeenTime
                        )
                        SmartGlassParticipantData.UserState.ONLINE -> setContactState(
                            ContactStateTextView.State.ONLINE
                        )
                    }
                }
            }
        })

        bottomActionBar!!.setTapOnClickListener {
            if(currentParticipantIndex == -1) return@setTapOnClickListener
            val action =
                ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(
                    participantsData[currentParticipantIndex]
                )
            findNavController().navigate(action)
        }

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

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.smoothScrollToNext(currentParticipantIndex)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY && currentParticipantIndex != -1) {
                rvParticipants!!.smoothScrollToPrevious(currentParticipantIndex)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.TAP -> {
            if(currentParticipantIndex != -1) {
                val action =
                    ParticipantsFragmentDirections.actionParticipantsFragmentToParticipantDetailsFragment(
                        participantsData[currentParticipantIndex]
                    )
                findNavController().navigate(action)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}