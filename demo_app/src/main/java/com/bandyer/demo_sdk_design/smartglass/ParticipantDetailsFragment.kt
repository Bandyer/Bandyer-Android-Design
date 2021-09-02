package com.bandyer.demo_sdk_design.smartglass

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.*

class ParticipantDetailsFragment : SmartGlassParticipantDetailsFragment(), TiltController.TiltListener {

    private val activity by lazy { requireActivity() as SmartGlassActivity }
    private val args: ParticipantDetailsFragmentArgs by navArgs()

    private var tiltController: TiltController? = null

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

        val data = args.participantData!!

        avatar!!.setText(data.name.first().toString())
        avatar!!.setBackground(data.name.parseToColor())
        name!!.text = data.name
        with(contactStateText!!) {
            when (data.userState) {
                SmartGlassParticipantData.UserState.INVITED -> setContactState(
                    ContactStateTextView.State.INVITED
                )
                SmartGlassParticipantData.UserState.OFFLINE -> setContactState(
                    ContactStateTextView.State.LAST_SEEN,
                    data.lastSeenTime
                )
                else -> setContactState(
                    ContactStateTextView.State.ONLINE
                )
            }
        }
        when {
            data.avatarImageId != null -> avatar!!.setImage(data.avatarImageId)
            data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl!!)
            else -> avatar!!.setImage(null)
        }
        contactStateDot!!.isActivated = data.userState == SmartGlassParticipantData.UserState.ONLINE

        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_videocall)))
        itemAdapter!!.add(ParticipantDetailsItem(resources.getString(R.string.bandyer_smartglass_call)))

        bottomActionBar!!.setSwipeDownOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onTilt(x: Float, y: Float) = rvActions!!.scrollBy((x * 40).toInt(), 0)

    override fun onResume() {
        super.onResume()
        activity.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.bandyer_smartglass_background_color, null))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.requestAllSensors()
    }

    override fun onStop() {
        super.onStop()
        activity.setStatusBarColor(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tiltController!!.releaseAllSensors()
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when(event.type) {
        SmartGlassTouchEvent.Type.SWIPE_DOWN -> {
            findNavController().popBackStack()
            true
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}